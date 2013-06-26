import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {
  val appName         = "acmss"
  val appVersion      = "1.0"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "com.typesafe.play" %% "play-slick" % "0.3.2",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "com.github.tototoshi" %% "slick-joda-mapper" % "0.2.1"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
  )
}
