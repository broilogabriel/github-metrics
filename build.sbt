ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

ThisBuild / scalafmtOnCompile := true

val CatsEffectsVersion    = "3.5.4"
val Http4sVersion         = "0.23.26"
val GitHub4sVersion       = "0.33.3"
val Fs2CronCron4sVersion  = "0.9.0"
val CirceVersion          = "0.14.6"
val DoobieVersion         = "1.0.0-RC4"
val PureConfigVersion     = "0.17.6"
val Log4CatsVersion       = "2.6.0"
val LogbackClassicVersion = "1.5.6"
val ScalatestVersion      = "3.2.18"

lazy val root = (project in file("."))
  .settings(
    name := "github-metrics",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"           % CatsEffectsVersion,
      "org.http4s"            %% "http4s-ember-client"   % Http4sVersion,
      "org.http4s"            %% "http4s-ember-server"   % Http4sVersion,
      "org.http4s"            %% "http4s-circe"          % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"            % Http4sVersion,
      "com.47deg"             %% "github4s"              % GitHub4sVersion,
      "eu.timepit"            %% "fs2-cron-cron4s"       % Fs2CronCron4sVersion,
      "io.circe"              %% "circe-generic"         % CirceVersion,
      "org.tpolecat"          %% "doobie-core"           % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"         % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"       % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres-circe" % DoobieVersion,
      "com.github.pureconfig" %% "pureconfig"            % PureConfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"        % Log4CatsVersion,
      "ch.qos.logback"         % "logback-classic"       % LogbackClassicVersion,
      "org.scalactic"         %% "scalactic"             % ScalatestVersion,
      "org.scalatest"         %% "scalatest"             % ScalatestVersion % Test
    )
  )
