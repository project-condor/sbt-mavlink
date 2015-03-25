package com.github.jodersky.mavlink

import scala.xml.XML
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.File

import trees._

/**
 * Generates Scala code implementing the MAVLink protocol.
 * @param dialect a specific MAVLink dialect for which to generate code
 * @param name name of the dialect
 */
class Generator(dialect: Dialect, name: String) {
  import Generator._
  
  lazy val maxPayloadLength = dialect.messages.map(_.length).max

  lazy val extraCrcs = Array.tabulate[Byte](255){i =>
    val message = dialect.messages.find(_.id == i)
    message.map(_.checksum).getOrElse(0)
  }

  /**
   * Represents a generator's target file
   * @param path the path of the generated file
   * @param generate contents of the generated file
   */
  case class Target(path: String, generate: () => String)

  def targets: List[Target] = {
    val context = Context(
      dialect.version,
      name
    )
    List(
      Target(targetFiles(0), () => org.mavlink.txt.Assembler(context).body),
      Target(targetFiles(1), () => org.mavlink.txt.Crc(context).body),
      Target(targetFiles(2), () => org.mavlink.txt.Mavlink(context).body),
      Target(targetFiles(3), () => org.mavlink.txt.Packet(context, maxPayloadLength, extraCrcs).body),
      Target(targetFiles(4), () => org.mavlink.txt.Parser(context).body),
      Target(targetFiles(5), () => org.mavlink.messages.txt.messages(context, dialect.messages).body),
      Target(targetFiles(6), () => org.mavlink.enums.txt.enums(context, dialect.enums).body)
    )
  }

}

object Generator {

  val targetFiles: Seq[String] = Array(
    "org/mavlink/Assembler.scala",
    "org/mavlink/Crc.scala",
    "org/mavlink/Mavlink.scala",
    "org/mavlink/Packet.scala",
    "org/mavlink/Parser.scala",
    "org/mavlink/messages/messages.scala",
    "org/mavlink/enums/enums.scala"
  )

}