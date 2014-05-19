import sbt._
import sbt.Keys._
import play.Project._

play.Project.playScalaSettings

name         := "SlickChair"

version      := "0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json"                   % "2.2.0",
  "com.typesafe.play" %% "play-jdbc"                   % "2.2.0",
  "com.typesafe.play" %% "anorm"                       % "2.2.0",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "ws.securesocial" %% "securesocial" % "2.1.3",
  "joda-time" % "joda-time" % "2.3",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.ocpsoft.prettytime" % "prettytime" % "3.2.5.Final",
  filters)

resolvers ++= Seq(
  "webjars" at "http://webjars.github.com/m2",
  Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("Sonatype Snapshots",url("http://oss.sonatype.org/content/repositories/snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),
  "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
  "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/")

routesImport ++= Seq(
  "models._",
  "controllers.Utils.IdBindable")

templatesImport ++= Seq(
  "models._",
  "controllers._",
  "bootstrap3._")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-target:jvm-1.6",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Ywarn-value-discard",
  "-Xlint")
