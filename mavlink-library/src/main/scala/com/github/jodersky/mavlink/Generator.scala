package com.github.jodersky.mavlink

import scala.xml.XML
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.File

import trees._

/**
 * Generates Scala code implementing the MAVLink protocol.
 * @param dialect a specific MAVLink dialect for which to generate code
 */
class Generator(dialect: Dialect) {
  
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
    val context = Context(dialect.version)
    List(
      Target("org/mavlink/Assembler.scala", () => org.mavlink.txt.Assembler(context).body),
      Target("org/mavlink/Crc.scala", () => org.mavlink.txt.Crc(context).body),
      Target("org/mavlink/Packet.scala", () => org.mavlink.txt.Packet(context, maxPayloadLength, extraCrcs).body),
      Target("org/mavlink/Parser.scala", () => org.mavlink.txt.Parser(context).body),
      Target("org/mavlink/messages/messages.scala", () => org.mavlink.messages.txt.messages(context, dialect.messages).body)
    )
  }

}