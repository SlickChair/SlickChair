import sbt._
import sbt.Keys._
import play.Project._

play.Project.playScalaSettings

name         := "SlickChair"

version      := "0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.play"       %% "play-json"                   % "2.2.0",
  "com.typesafe.play"       %% "play-jdbc"                   % "2.2.0",
  "com.typesafe.play"       %% "anorm"                       % "2.2.0",
                                                                 // 
  "com.typesafe.play" %% "play-slick" % "0.5.0.7",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
                                                                 // 
  "com.github.tototoshi" %% "slick-joda-mapper" % "0.2.1",
  // "org.scala-tools.time" % "time_2.9.1" % "0.5",
                                                                 // 
  "securesocial" %% "securesocial" % "master-SNAPSHOT"
)

resolvers ++= Seq(
  "webjars" at "http://webjars.github.com/m2",
  Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  //Resolver.file("Local Repository", file(sys.env.get("PLAY_HOME").map(_ + "/repository/local").getOrElse("")))(Resolver.ivyStylePatterns),
  Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("Sonatype Snapshots",url("http://oss.sonatype.org/content/repositories/snapshots/"))(Resolver.ivyStylePatterns),
  Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
  Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns),
  "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
  "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
)

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.6", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")
