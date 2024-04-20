ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

ThisBuild / scalafmtOnCompile := true

val CatsEffectsVersion = "3.5.4"
val Http4sVersion      = "0.23.26"
val CirceVersion       = "0.14.6"
val Log4CatsVersion    = "2.6.0"
val Slf4jVersion       = "2.0.13"
val ScalatestVersion   = "3.2.18"
val DoobieVersion      = "1.0.0-RC4"

lazy val root = (project in file("."))
  .settings(
    name := "github-metrics",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"           % CatsEffectsVersion,
      "org.http4s"    %% "http4s-ember-client"   % Http4sVersion,
      "org.http4s"    %% "http4s-ember-server"   % Http4sVersion,
      "org.http4s"    %% "http4s-circe"          % Http4sVersion,
      "org.http4s"    %% "http4s-dsl"            % Http4sVersion,
      "io.circe"      %% "circe-generic"         % CirceVersion,
      "org.tpolecat"  %% "doobie-core"           % DoobieVersion,
      "org.tpolecat"  %% "doobie-hikari"         % DoobieVersion,
      "org.tpolecat"  %% "doobie-postgres"       % DoobieVersion,
      "org.tpolecat"  %% "doobie-postgres-circe" % DoobieVersion,
      "org.tpolecat"  %% "doobie-specs2"         % DoobieVersion,
      "org.typelevel" %% "log4cats-slf4j"        % Log4CatsVersion,
      "org.slf4j"      % "slf4j-simple"          % Slf4jVersion,
      "org.scalactic" %% "scalactic"             % ScalatestVersion,
      "org.scalatest" %% "scalatest"             % ScalatestVersion % Test
    ),
    mainClass := Option("io.github.broilogabriel.Main")
  )
