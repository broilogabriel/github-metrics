package io.github.broilogabriel.github

import cats.effect._
import cats.implicits._
import github4s.GithubClient
import github4s.domain.PullRequest
import io.circe.Json
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.core.Config.GitHub
import io.github.broilogabriel.github.model.DeliveryId

sealed trait GitHubService[F[_]] {
  def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit]
  def synchronizePullRequests(owner: String, repo: String): F[List[PullRequest]]
}

object GitHubService {

  private final class Impl[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F])
      extends GitHubService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    private val gitHubClient = for {
      client <- EmberClientBuilder.default.build
    } yield GithubClient[F](client, accessToken = Option(config.token))

    override def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit] = for {
      _ <- logger.info(s"Got notification: $deliveryId")
      _ <- repository.saveNotification(deliveryId, data)
    } yield ()

    override def synchronizePullRequests(owner: String, repo: String): F[List[PullRequest]] = {
      val x: F[List[PullRequest]] = gitHubClient
        .use(_.pullRequests.listPullRequests(owner, repo))
        .map(_.result)
        .flatMap {
          case Left(err)    => err.raiseError[F, List[PullRequest]]
          case Right(value) => value.pure[F]
        }
      x
    }
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F]): GitHubService[F] =
      new Impl(config, repository)
  }

}
