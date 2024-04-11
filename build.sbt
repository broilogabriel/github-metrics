ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "template",
    version := "0.0.1",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.13",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "org.scalactic" %% "scalactic" % "3.2.18",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    )
  )
