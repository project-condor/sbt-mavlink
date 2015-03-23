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
    version := "0.3-SNAPSHOT"
  ) ++ publishSettings

  lazy val root = (
    Project("root", file("."))
    aggregate(
      library,
      plugin
    )
    settings(
      publish := (),
      publishLocal := ()
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
      publishLocal <<= publishLocal.dependsOn(publishLocal in library)
    )
    dependsOn(library)
  )
  
  lazy val publishSettings: Seq[Setting[_]] = Seq(
    licenses := Seq(("LGPL", url("http://opensource.org/licenses/LGPL-3.0"))),
    homepage := Some(url("http://github.com/jodersky/sbt-mavlink")),
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomIncludeRepository := { _ => false },
    pomExtra := {
      <scm>
        <url>git@github.com:jodersky/sbt-mavlink.git</url>
        <connection>scm:git:git@github.com:jodersky/sbt-mavlink.git</connection>
      </scm>
      <developers>
        <developer>
          <id>jodersky</id>
          <name>Jakob Odersky</name>
        </developer>
      </developers>
    }
  )

}

