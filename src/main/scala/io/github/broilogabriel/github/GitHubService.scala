package io.github.broilogabriel.github

import java.time.Instant

import scala.annotation.tailrec
import scala.collection.immutable.List
import scala.io.Source

import cats.effect._
import cats.implicits._
import github4s.GithubClient
import github4s.domain.{Pagination, PRFilterAll, PRFilterOrderAsc, PRFilterOrderDesc, PRFilterSortUpdated}
import io.circe.Json
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.core.Config.GitHub
import io.github.broilogabriel.github.model.{DeliveryId, PullRequest, Repository, User}
import io.github.broilogabriel.github.model.PullRequest.SynchronizedAt

sealed trait GitHubService[F[_]] {
  def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit]
  def synchronizePullRequests: F[List[(Repository, Int)]]
  def saveRepository(repo: Repository): F[Unit]
}

object GitHubService {

  private final class Impl[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F])
      extends GitHubService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    private val gitHubClient = for {
      client <- EmberClientBuilder.default.build
    } yield GithubClient[F](client, accessToken = Option(config.token))

    // TODO add rate limiter to client

    override def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit] = for {
      _ <- logger.info(s"Got notification: $deliveryId")
      _ <- repository.createNotification(deliveryId, data)
    } yield ()

    override def saveRepository(repo: Repository): F[Unit] = for {
      _ <- repository.createUser(repo.owner)
      _ <- repository.upsertRepository(repo)
    } yield ()

    private def fetchPullRequests(
      login: User.Login,
      name: Repository.Name,
      synchronizedAt: Option[Repository.SynchronizedAt]
    ): F[List[PullRequest]] = {
      def inner(
        pagination: Option[Pagination],
        pullRequests: List[PullRequest]
      ): F[List[PullRequest]] = {
        pagination match {
          case None => pullRequests.pure[F]
          case Some(Pagination(page, per_page)) =>
            for {
              _ <- logger.debug(s"WILL REQUEST WITH: $pagination")
              response <- gitHubClient
                .use(
                  _.pullRequests
                    .listPullRequests(
                      login.value,
                      name.value,
                      List(PRFilterAll, PRFilterSortUpdated, PRFilterOrderDesc),
                      pagination
                    )
                )
              prs <- response.result match {
                case Left(err)    => err.raiseError[F, List[PullRequest]]
                case Right(value) => value.map(PullRequest(_, SynchronizedAt(Instant.now()))).pure[F]
              }
              _ <- logger.debug(s"GOT HEADERS: ${response.headers.filter(p =>
                  Set(
                    "retry-after",
                    "x-ratelimit-remaining",
                    "x-ratelimit-reset",
                    "x-ratelimit-resource"
                  ).contains(p._1.toLowerCase)
                )}")
              _ <- logger.debug(s"GOT THIS: -> ${prs.headOption}")
              nextPage =
                if (prs.isEmpty || synchronizedAt.exists(_.value.isAfter(prs.flatMap(_.updatedAt.map(_.value)).min))) {
                  Option.empty[Pagination]
                } else {
                  Option(Pagination(page + 1, per_page))
                }
              result <- inner(nextPage, pullRequests ++ prs)
            } yield result
        }
      }
      inner(Option(Pagination(1, 100)), List.empty)
    }

    override def synchronizePullRequests: F[List[(Repository, Int)]] =
      repository
        .findAllRepositories()
        .evalMap { case repo @ Repository(_, owner, name, synchronizedAt) =>
          for {
            now      <- Instant.now().pure[F]
            prs      <- fetchPullRequests(owner.login, name, synchronizedAt)
            newUsers <- repository.createUsers(prs.map(_.user).distinct)
            _        <- logger.debug(s"Created $newUsers new users")
            saved    <- repository.createPullRequests(prs)
            _        <- logger.debug(s"Fetched ${prs.size} prs for ${owner.login.value}/${name.value}; saved $saved")
            updatedRepo = repo.copy(synchronizedAt = Option(Repository.SynchronizedAt(now)))
            _ <- repository.upsertRepository(updatedRepo)
          } yield (updatedRepo, saved)
        }
        .compile
        .toList
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F]): GitHubService[F] =
      new Impl(config, repository)
  }

}
