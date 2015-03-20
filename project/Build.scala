import sbt._
import sbt.Keys._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import._
import sbt.ScriptedPlugin._

object ApplicationBuild extends Build {

  val common = Seq(
    scalaVersion := "2.10.4",
    scalacOptions ++= Seq("-feature", "-deprecation"),
    organization := "com.github.jodersky",
    version := "0.1-SNAPSHOT"
  )

  lazy val root = Project("root", file(".")).aggregate(
    library,
    plugin
  )

  lazy val library = (
    Project("mavlink-library", file("mavlink-library"))
    enablePlugins(SbtTwirl)
    settings(common: _*)
    settings(
      libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
      TwirlKeys.templateImports += "com.github.jodersky.mavlink._",
      TwirlKeys.templateImports += "com.github.jodersky.mavlink.trees._"
    )
  )

  lazy val plugin = (
    Project("mavlink-plugin", file("mavlink-plugin"))
    settings(common: _*)
    settings(ScriptedPlugin.scriptedSettings: _*)
    settings(
      sbtPlugin := true,
      name := "sbt-mavlink",
      scriptedLaunchOpts := { scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false,
      publishLocal <<= publishLocal.dependsOn(publishLocal in library)
    )
    dependsOn(library)
  )

}

