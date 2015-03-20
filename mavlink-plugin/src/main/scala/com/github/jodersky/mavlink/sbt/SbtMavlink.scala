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

    val dialectDefinition = XML.loadFile(dialectDefinitionFile)
    val dialect = Parser.parseDialect(dialectDefinition)
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
