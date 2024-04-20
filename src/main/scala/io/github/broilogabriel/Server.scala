package io.github.broilogabriel

import cats.effect.{Async, Resource}
import cats.implicits._
import com.comcast.ip4s.IpLiteralSyntax
import fs2.io.net.Network
import io.github.broilogabriel.github.{GitHubRoutes, GitHubService}
import io.github.broilogabriel.projects.{ProjectsRoutes, ProjectsService}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

object Server {

  def run[F[_]: LoggerFactory: Async: Network]: F[Nothing] = {
    for {
      _ <- EmberClientBuilder.default[F].build
      httpApp = Router(
        "/" -> (ProjectsRoutes[F](ProjectsService.Impl()).routes <+>
          GitHubRoutes[F](GitHubService.Impl()).routes)
      ).orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
      _ <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }.useForever

}
