package com.github.jodersky.mavlink.sbt

import com.github.jodersky.mavlink.Parser
import com.github.jodersky.mavlink.Generator
import scala.xml.XML

import MavlinkKeys._
import sbt._
import sbt.Keys._
import sbt.plugins._

object SbtMavlink extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = JvmPlugin //this is required as sourceGenerators are otherwise reset

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    mavlinkDialect in Compile := (baseDirectory in Compile).value / "conf" / "mavlink.xml",
    mavlinkTarget in Compile := (sourceManaged in Compile).value,
    mavlinkGenerate in Compile := generationTask.value,
    sourceGenerators in Compile += (mavlinkGenerate in Compile).taskValue
  )

  lazy val generationTask = Def.task[Seq[File]] {
    val dialectDefinitionFile = (mavlinkDialect in Compile).value

    if (!dialectDefinitionFile.exists) sys.error(
      "Dialect definition " + dialectDefinitionFile.getAbsolutePath + " does not exist."
    )

    val dialectDefinition = XML.loadFile(dialectDefinitionFile)
    val dialect = Parser.parseDialect(dialectDefinition)
    val pathToSource = (new Generator(dialect)).generate()

    val outDirectory = (mavlinkTarget in Compile).value

    streams.value.log.info("Generating mavlink files...")

    val files = for ((path, source) <- pathToSource) yield {
      streams.value.log.debug("Generating " + path)
      val file = outDirectory / path
      IO.write(file, source)
      file.getAbsoluteFile
    }
    
    streams.value.log.info("Done generating mavlink files")
    files
  }

}
