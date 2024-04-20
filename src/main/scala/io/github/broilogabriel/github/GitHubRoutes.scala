package io.github.broilogabriel.github

import cats.effect._
import cats.syntax.all._
import io.circe.Json
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.Router
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

final class GitHubRoutes[F[_]: Concurrent: LoggerFactory](service: GitHubService[F]) extends Http4sDsl[F] {
  val logger: SelfAwareStructuredLogger[F]     = LoggerFactory[F].getLogger
  implicit val decoder: EntityDecoder[F, Json] = circe.jsonOf[F, Json]
  private val webhook: HttpRoutes[F] = HttpRoutes.of[F] { case r @ POST -> Root =>
    for {
      json     <- r.as[Json]
      _        <- service.saveNotification(json)
      response <- Created()
    } yield response
  }

  val routes: HttpRoutes[F] = Router(
    ("/github", webhook)
  )

}

object GitHubRoutes {
  def apply[F[_]: Concurrent: LoggerFactory](service: GitHubService[F]): GitHubRoutes[F] = new GitHubRoutes(service)
}
