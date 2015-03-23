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
    mavlinkTarget := (sourceManaged in Compile).value,
    mavlinkGenerate := generationTask.value,
    sourceGenerators in Compile += mavlinkGenerate.taskValue
  )

  lazy val generationTask = Def.task[Seq[File]] {
    val dialectDefinitionFile = mavlinkDialect.value
    val outDirectory = mavlinkTarget.value

    if (!dialectDefinitionFile.exists) sys.error(
      "Dialect definition " + dialectDefinitionFile.getAbsolutePath + " does not exist."
    )

    val reporter = new Reporter {
      def printWarning(msg: String) = streams.value.log.warn(msg)
    }

    val targetFiles = Generator.targetFiles map (outDirectory / _)

    if (targetFiles forall (_.lastModified > dialectDefinitionFile.lastModified)) {
      targetFiles map (_.getAbsoluteFile)
    } else {
      val dialectDefinition = XML.loadFile(dialectDefinitionFile)
      val dialect = (new Parser(reporter)).parseDialect(dialectDefinition)
      val targets = (new Generator(dialect)).targets
      for (tgt <- targets) yield {
        val file = (outDirectory / tgt.path)

        if (dialectDefinitionFile.lastModified > file.lastModified) {
          streams.value.log.info("Generating mavlink binding " + file)
          IO.write(file, tgt.generate())
        }
        file.getAbsoluteFile
      }
    }
  }

}
