package com.github.jodersky.mavlink

import java.io.File

import scala.language.postfixOps

import scala.xml._
import scala.util.Try

import trees._

/**
 * Provides means to parse a MAVLink dialect definition into a
 * scala object representation.
 */
class Parser(reporter: Reporter) {
  import reporter._

  def parseDialect(dialectDefinitionFile: File): Dialect = {
    val xml = XML.loadFile(dialectDefinitionFile)
    parseDialect(xml, dialectDefinitionFile)
  }

  private def parseDialect(node: Node, file: File): Dialect = parse(node, file) match {
    case p: Dialect => p
    case _ => fatal("expected mavlink protocol definition", node, file)
  } 
  
  def parse(node: Node, file: File): Tree = node match {
    case <field>{_*}</field> =>
      val description = node.text
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for field", node, file)
      val enum = (node \ "@enum").map(_.text).headOption
      val (tpe, native) = (node \ "@type") map (_.text) headOption match {
        case Some(t) => (parseType(t, node, file), t)
        case None => fatal("no field type specified", node, file)
      }
      Field(tpe, native, name, enum, description)

    case <entry>{_*}</entry> =>
      val value = (node \ "@value").map(_.text).headOption map { str =>
        Try { Integer.parseInt(str) } getOrElse fatal("value must be an integer", node, file)
      } getOrElse fatal("no value defined", node, file)
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for enum entry", node, file)
      val description = (node \ "description").text
      EnumEntry(value, name, description)

    case <enum>{_*}</enum> =>
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for enum", node, file)
      val description = (node \ "description").map(_.text).headOption getOrElse ""
      val entries = (node \ "entry").zipWithIndex map { case (n, i) =>

        //FIXME: some official MAVLink dialects don't define values in enums
        val nodeWithValue = if ((n \ "@value").isEmpty) {
          warn("no value defined for enum entry, using index instead", n, file)
          n.asInstanceOf[Elem] % Attribute(None, "value", Text(i.toString), Null)
        } else {
          n
        }

        parse(nodeWithValue, file) match {
          case e: EnumEntry => e
          case _ => fatal("illegal definition in enum, only entries are allowed", n, file)
        }
      }
      Enum(name, entries, description)

    case <message>{_*}</message> =>
      val id = (node \ "@id").map(_.text).headOption map { str =>
        val id = Try { Integer.parseInt(str) } getOrElse fatal("id must be an integer", node, file)
        if (id < 0 || id > 255) warn("message id is not in the range [0-255]", node, file)
        id.toByte
      } getOrElse fatal("no id defined", node, file)
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for message", node, file)
      val description = (node \ "description").text

      val fields = (node \ "field") map { n: Node =>
        parse(n, file) match {
          case e: Field => e
          case _ => fatal("illegal definition in message, only fields are allowed", n, file)
        }
      }
      Message(id, name, description, fields)

    case <mavlink>{_*}</mavlink> =>
      val version = (node \ "version").headOption.map(_.text)

      val include = (node \ "include").headOption.map(_.text).map { includeFileName =>
        val includeFile: File = new File(file.getParentFile, includeFileName)
        parseDialect(includeFile)
      }

      val enums = (node \ "enums" \ "_").toSet map { n: Node =>
        parse(n, file) match {
          case e: Enum => e
          case _ => fatal("illegal definition in enums, only enum declarations are allowed", n, file)
        }
      }

      val messages = (node \ "messages" \ "_").toSet map { n: Node =>
        parse(n, file) match {
          case e: Message => e
          case e => fatal("illegal definition in messages, only message declarations are allowed", n, file)
        }
      }

      include match {
        case None => Dialect(version, enums, messages)
        case Some(includeDialect) => Dialect(
          (version ++ includeDialect.version).headOption, // included version overridden by local version if any
          enums ++ includeDialect.enums,
          messages ++ includeDialect.messages
        )
      }

      
    case x => fatal("unknown", x, file)

  }
  
  val ArrayPattern = """(.*)\[(\d+)\]""".r
  def parseType(typeStr: String, node: Node, file: File): Type = typeStr match {
    case "int8_t" => IntType(1, true)
    case "uint8_t_mavlink_version" => IntType(1, false)
    case "int16_t" => IntType(2, true)
    case "int32_t" => IntType(4, true)
    case "int64_t" => IntType(8, true)
    case "uint8_t" => IntType(1, false)
    case "uint16_t" => IntType(2, false)
    case "uint32_t" => IntType(4, false)
    case "uint64_t" => IntType(8, false)
    case "float" => FloatType(4)
    case "double" => FloatType(8)
    case "char" => CharType
    case ArrayPattern("char", l) => StringType(l.toInt)
    case ArrayPattern(u, l) => ArrayType(parseType(u, node, file), l.toInt)
    case unknown => fatal("unknown field type " + unknown, node, file)
  }

}
