package io.github.broilogabriel

import cats.ApplicativeError
import cats.effect._
import cats.effect.implicits._
import cats.effect.syntax._
import cats.implicits._
import cats.syntax._
import com.comcast.ip4s.IpLiteralSyntax
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.LoggerFactory
import pureconfig.{ConfigSource, _}
import pureconfig.generic.auto._

import io.github.broilogabriel.core._

object Server {

  def run[F[_]: LoggerFactory: Async: Network]: F[Nothing] = {
    implicit val config: Config = ConfigSource.default.loadOrThrow[Config]

    for {
      _        <- EmberClientBuilder.default[F].build
      database <- Database("localhost", 54320, "github-metrics", "docker", "docker", "github-metrics")
      repo         = Repository(database)
      service      = Service(config, repo)
      httpApp      = Routes(service).endpoints
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(httpApp)
      _ <- Scheduler(config, service).start.background
      _ <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }.useForever

}
