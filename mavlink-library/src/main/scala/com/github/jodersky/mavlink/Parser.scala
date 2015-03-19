package com.github.jodersky.mavlink

import scala.language.postfixOps

import scala.xml.Node
import scala.util.Try

import trees._

/**
 * Provides means to parse a MAVLink dialect definition into a
 * scala object representation.
 */
object Parser {

  def fatal(error: String, node: Node) = throw new ParseError("Parse error at " + node + ": " + error)
  def warn(warning: String, node: Node) = Console.err.println("Warning " + node + ": " + warning)

  def parseDialect(node: Node): Dialect = parse(node) match {
    case p: Dialect => p
    case _ => fatal("expected mavlink protocol definition", node)
  } 
  
  def parse(node: Node): Tree = node match {
    case <field>{_*}</field> =>
      val description = node.text
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for field", node)
      val enum = (node \ "@enum").map(_.text).headOption
      val (tpe, native) = (node \ "@type") map (_.text) headOption match {
        case Some(t) => (parseType(t, node), t)
        case None => fatal("no field type specified", node)
      }
      Field(tpe, native, name, enum, description)

    case <entry>{_*}</entry> =>
      val value = (node \ "@value").map(_.text).headOption map { str =>
        Try { Integer.parseInt(str) } getOrElse fatal("value must be an integer", node)
      } getOrElse fatal("no value defined", node)
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for enum entry", node)
      val description = (node \ "description").text
      EnumEntry(value, name, description)

    case <enum>{_*}</enum> =>
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for enum", node)
      val entries = (node \ "entry").toSet map { n: Node =>
        parse(n) match {
          case e: EnumEntry => e
          case _ => fatal("illegal definition in enum, only entries are allowed", n)
        }
      }
      Enum(name, entries)

    case <message>{_*}</message> =>
      val id = (node \ "@id").map(_.text).headOption map { str =>
        val id = Try { Integer.parseInt(str) } getOrElse fatal("id must be an integer", node)
        if (id < 0 || id > 255) warn("message id is not in the range [0-255]", node)
        id.toByte
      } getOrElse fatal("no id defined", node)
      val name = (node \ "@name").map(_.text).headOption getOrElse fatal("no name defined for message", node)
      val description = (node \ "description").text

      val fields = (node \ "field") map { n: Node =>
        parse(n) match {
          case e: Field => e
          case _ => fatal("illegal definition in message, only fields are allowed", n)
        }
      }
      Message(id, name, description, fields)

    case <mavlink>{_*}</mavlink> =>
      val version = (node \ "version").text

      val enums = (node \ "enums" \ "_").toSet map { n: Node =>
        parse(n) match {
          case e: Enum => e
          case _ => fatal("illegal definition in enums, only enum declarations are allowed", n)
        }
      }

      val messages = (node \ "messages" \ "_").toSet map { n: Node =>
        parse(n) match {
          case e: Message => e
          case e => fatal("illegal definition in messages, only message declarations are allowed", n)
        }
      }
      Dialect(version, enums, messages)
      
    case x => fatal("unknown", x)

  }
  
  val ArrayPattern = """(.*)\[(\d+)\]""".r
  def parseType(typeStr: String, node: Node): Type = typeStr match {
    case "int8_t" => IntType(1, true)
    case "int16_t" => IntType(2, true)
    case "int32_t" => IntType(4, true)
    case "int64_t" => IntType(8, true)
    case "uint8_t" => IntType(1, false)
    case "uint16_t" => IntType(2, false)
    case "uint32_t" => IntType(4, false)
    case "uint64_t" => IntType(8, false)
    case "float" => FloatType(4)
    case "double" => FloatType(8)
    case "char" => IntType(1, true)
    case ArrayPattern("char", l) => StringType(l.toInt)
    case ArrayPattern(u, l) => ArrayType(parseType(u, node), l.toInt)
    case unknown => fatal("unknown field type " + unknown, node)
  }

}