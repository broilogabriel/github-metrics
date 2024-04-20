package io.github.broilogabriel.github

import cats.effect._
import cats.syntax.all._
import io.circe.Json
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.github.headers._
import io.github.broilogabriel.github.model.DeliveryId

final class GitHubRoutes[F[_]: Concurrent: LoggerFactory](service: GitHubService[F]) extends Http4sDsl[F] {
  val logger: SelfAwareStructuredLogger[F]     = LoggerFactory[F].getLogger
  implicit val decoder: EntityDecoder[F, Json] = circe.jsonOf[F, Json]
  private val webhook: HttpRoutes[F] = HttpRoutes.of[F] { case r @ POST -> Root =>
    // TODO define error handling
    for {
      deliveryIdHeader <- Concurrent[F].fromOption(
        r.headers.get[`X-GitHub-Delivery`],
        new Throwable("Missing header `X-GitHub-Delivery`")
      )
      json     <- r.as[Json]
      _        <- service.saveNotification(deliveryIdHeader.to(DeliveryId), json)
      response <- Created(deliveryIdHeader.value.toString)
    } yield response
  }

  val routes: HttpRoutes[F] = Router(
    ("/github", webhook)
  )

}

object GitHubRoutes {
  def apply[F[_]: Concurrent: LoggerFactory](service: GitHubService[F]): GitHubRoutes[F] = new GitHubRoutes(service)
}
