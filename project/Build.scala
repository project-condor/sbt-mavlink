import sbt._
import sbt.Keys._
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import._
import sbt.ScriptedPlugin._
import bintray.BintrayPlugin.autoImport._

object ApplicationBuild extends Build {

  val common = Seq(
    scalaVersion := "2.10.6",
    scalacOptions ++= Seq("-feature", "-deprecation"),
    organization := "com.github.jodersky",
    version := "0.6.0",
    licenses := Seq(("LGPL", url("http://opensource.org/licenses/LGPL-3.0")))
  )

  lazy val root = (
    Project("root", file("."))
    aggregate(
      library,
      plugin
    )
    settings(
      publish := (),
      publishLocal := (),
      publishTo := Some(Resolver.file("Unused transient repository", target.value / "unusedrepo")) // make sbt-pgp happy
    )
  )

  lazy val library = (
    Project("mavlink-library", file("mavlink-library"))
    enablePlugins(SbtTwirl)
    settings(common: _*)
    settings(
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
      publishMavenStyle := false,
      bintrayRepository := "sbt-plugins",
      bintrayOrganization in bintray := None
    )
    dependsOn(library)
  )

}

