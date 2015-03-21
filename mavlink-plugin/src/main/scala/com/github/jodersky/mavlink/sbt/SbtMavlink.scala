package com.github.jodersky.mavlink.sbt

import MavlinkKeys._

import com.github.jodersky.mavlink.Generator
import com.github.jodersky.mavlink.Parser
import com.github.jodersky.mavlink.Reporter

import sbt._
import sbt.Keys._
import sbt.plugins._

import scala.xml.XML

object SbtMavlink extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = JvmPlugin //this is required as sourceGenerators are otherwise reset

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    mavlinkDialect := baseDirectory.value / "conf" / "mavlink.xml",
    mavlinkTarget := sourceManaged.value,
    mavlinkGenerate := generationTask.value,
    sourceGenerators in Compile += mavlinkGenerate.taskValue
  )

  lazy val generationTask = Def.task[Seq[File]] {
    val dialectDefinitionFile = mavlinkDialect.value

    if (!dialectDefinitionFile.exists) sys.error(
      "Dialect definition " + dialectDefinitionFile.getAbsolutePath + " does not exist."
    )

    val reporter = new Reporter {
      def printWarning(msg: String) = streams.value.log.warn(msg)
    }

    val dialectDefinition = XML.loadFile(dialectDefinitionFile)
    val dialect = (new Parser(reporter)).parseDialect(dialectDefinition)
    val pathToSource = (new Generator(dialect)).generate()

    val outDirectory = mavlinkTarget.value

    streams.value.log.info("Generating mavlink files...")

    val files = for ((path, source) <- pathToSource) yield {
      val file = outDirectory / path
      streams.value.log.info("Generating " + file)
      IO.write(file, source)
      file.getAbsoluteFile
    }
    
    streams.value.log.info("Done generating mavlink files")
    files
  }

}
