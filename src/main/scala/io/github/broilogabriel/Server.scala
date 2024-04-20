package io.github.broilogabriel

import cats.effect._
import com.comcast.ip4s.IpLiteralSyntax
import fs2.io.net.Network
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.core._

object Server {

  def run[F[_]: LoggerFactory: Async: Network]: F[Nothing] = {
    for {
      _        <- EmberClientBuilder.default[F].build
      database <- Database("", "", "")
      repo         = Repository(database)
      service      = Service(repo)
      httpApp      = Routes(service).endpoints
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
      //      httpApp = Router(
      //        "/" -> (ProjectsRoutes[F](ProjectsService.Impl()).routes <+>
      //          GitHubRoutes[F](GitHubService.Impl()).routes)
      //      ).orNotFound
      _ <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
    } yield ()
  }.useForever

}
