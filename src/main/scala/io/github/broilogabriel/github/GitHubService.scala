package io.github.broilogabriel.github

import cats.effect._
import cats.implicits._
import github4s.GithubClient
import github4s.domain.{PRFilterAll, PullRequest}
import io.circe.Json
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.core.Config.GitHub
import io.github.broilogabriel.github.model.{DeliveryId, Repository}

sealed trait GitHubService[F[_]] {
  def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit]
  def synchronizePullRequests: F[List[PullRequest]]
  def saveRepository(repo: Repository): F[Unit]
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

    override def saveRepository(repo: Repository): F[Unit] = for {
      _ <- repository.createUser(repo.owner)
      _ <- repository.createRepository(repo)
    } yield ()

    override def synchronizePullRequests: F[List[PullRequest]] = {
      repository
        .findAllRepositories()
        .evalMap { repo =>
          val fetchPRs: F[List[PullRequest]] = gitHubClient
            .use(
              _.pullRequests.listPullRequests(repo.owner.login.value, repo.name.value, List(PRFilterAll))
            )
            .map(_.result)
            .flatMap {
              case Left(err)    => err.raiseError[F, List[PullRequest]]
              case Right(value) => value.pure[F]
            }
          for {
            prs <- fetchPRs
            _   <- logger.info(s"Fetched ${prs.size} prs for ${repo.owner.login.value}/${repo.name.value}")
          } yield prs
        }
        .map(_.head)
        .compile
        .toList
    }
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F]): GitHubService[F] =
      new Impl(config, repository)
  }

}
