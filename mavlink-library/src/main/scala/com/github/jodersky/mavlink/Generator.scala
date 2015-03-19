package com.github.jodersky.mavlink

import scala.xml.XML
import java.io.FileWriter
import java.io.BufferedWriter
import scalax.file.Path
import java.io.File

import trees._

/**
 * Generates Scala code implementing the MAVLink protocol.
 * @param dialect a specific MAVLink dialect for which to generate code
 */
class Generator(dialect: Dialect) {
  
  lazy val maxPayloadLength = {
    val widths = dialect.messages map { msg =>
      msg.fields.map(_.tpe.sizeof).sum
    }
    widths.max
  }

  lazy val extraCrcs = Array.tabulate[Byte](255){i =>
    val message = dialect.messages.find(_.id == i)
    message.map(_.checksum).getOrElse(0)
  }

  /**
   * Generates Scala code implementing MAVLink.
   * @return a list containing proposed Scala file names pointing to their contents
   */
  def generate(): List[(String, String)] = {
    val context = Context(dialect.version)

    List(
      "org/mavlink/Assembler.scala" -> org.mavlink.txt.Assembler(context).body,
      "org/mavlink/Crc.scala" -> org.mavlink.txt.Crc(context).body,
      "org/mavlink/MavlinkBuffer.scala" -> org.mavlink.txt.MavlinkBuffer(context).body,
      "org/mavlink/Packet.scala" -> org.mavlink.txt.Packet(context, maxPayloadLength, extraCrcs).body,
      "org/mavlink/Parser.scala" -> org.mavlink.txt.Parser(context).body,
      "org/mavlink/messages/messages.scala" -> org.mavlink.messages.txt.messages(context, dialect.messages).body
    )
  }
}