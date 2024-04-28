package io.github.broilogabriel.github

import cats.effect._
import cats.effect.syntax.all._
import cats.implicits.catsSyntaxMonadError
import cats.syntax.all._
import io.circe.Json
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.github.headers._
import io.github.broilogabriel.github.headers.`X-GitHub-Event`.PULL_REQUEST
import io.github.broilogabriel.github.model.{DeliveryId, PullRequest, PullRequestEvent, Repository}

final class GitHubRoutes[F[_]: Concurrent: LoggerFactory](service: GitHubService[F]) extends Http4sDsl[F] {
  val logger: SelfAwareStructuredLogger[F]     = LoggerFactory[F].getLogger
  implicit val decoder: EntityDecoder[F, Json] = circe.jsonOf[F, Json]
  private val webhook: HttpRoutes[F] = HttpRoutes.of[F] { case r @ POST -> Root =>
    for {
      eventType <- r.headers
        .get[`X-GitHub-Event`]
        .pure
        .ensure(new Throwable(s"Missing header ${`X-GitHub-Event`.getClass.getSimpleName}"))(_.nonEmpty)
      deliveryIdHeader <- r.headers
        .get[`X-GitHub-Delivery`]
        .pure
        .ensure(new Throwable(s"Missing header ${`X-GitHub-Delivery`.getClass.getSimpleName}"))(_.nonEmpty)
        .map(_.get)
      response <- eventType match {
        case Some(PULL_REQUEST) =>
          for {
            json <- r.as[Json]
            pr <- json.hcursor
              .as[PullRequestEvent](PullRequestEvent.ExternalDecoder)
              .leftMap(_.fillInStackTrace())
              .liftTo[F]
            _        <- service.updatePullRequestWebhook(deliveryIdHeader.to(DeliveryId), pr)
            response <- Created()
          } yield response
        case _ => Accepted()
      }
    } yield response
  }
  private val syncPR: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    for {
      _ <- service.synchronizePullRequests
      r <- Accepted()
    } yield r
  }
  private val monitor: HttpRoutes[F] = HttpRoutes.of[F] { case json @ POST -> Root / "monitor" =>
    for {
      repo     <- json.as[Repository]
      response <- service.saveRepository(repo)
      r        <- Ok(response)
    } yield r
  }

  val routes: HttpRoutes[F] = Router(
    ("/github", webhook <+> syncPR <+> monitor)
  )

}

object GitHubRoutes {
  def apply[F[_]: Concurrent: LoggerFactory](service: GitHubService[F]): GitHubRoutes[F] = new GitHubRoutes(service)
}
