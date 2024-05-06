package io.github.broilogabriel.github

import java.time.Instant

import cats.effect._
import cats.implicits._
import github4s.GithubClient
import github4s.domain.{Pagination, PRFilterAll, PRFilterOrderDesc, PRFilterSortUpdated}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.core.Config.GitHub
import io.github.broilogabriel.github.model._
import io.github.broilogabriel.github.model.PullRequest.SynchronizedAt

sealed trait GitHubService[F[_]] {
  def updatePullRequestWebhook(deliveryId: DeliveryId, data: PullRequestEvent): F[Int]
  def synchronizePullRequests: F[Unit]
  def saveRepository(repo: Repository): F[Unit]
}

object GitHubService {

  private[github] val FIRST_PAGE = Pagination(1, 100)

  private[github] def getNextPage(
    currentPage: Pagination,
    synchronizedAt: Option[Repository.SynchronizedAt],
    pullRequests: Seq[_ <: PullRequest]
  ): Option[Pagination] = for {
    oldestUpdate <- pullRequests.flatMap(_.updatedAt.map(_.value)).minOption
    lastSync     <- synchronizedAt.map(_.value).orElse(Option(Instant.MIN))
    _nextPage    <- Option.when(lastSync.isBefore(oldestUpdate))(currentPage.copy(page = currentPage.page + 1))
  } yield _nextPage

  private final class Impl[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F])
      extends GitHubService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    private val gitHubClient = for {
      client <- EmberClientBuilder.default.build
    } yield GithubClient[F](client, accessToken = Option(config.token))

    // TODO add rate limiter to client

    override def updatePullRequestWebhook(deliveryId: DeliveryId, data: PullRequestEvent): F[Int] = for {
      _    <- logger.debug(s"Got notification: $deliveryId - ${data.action}")
      _    <- repository.createNotification(deliveryId, data)
      _    <- repository.createUser(data.sender)
      _    <- repository.upsertRepository(data.repository)
      rows <- repository.upsertPullRequestWebhook(data.pullRequest)
      _    <- logger.debug(s"Updated: $rows")
    } yield rows

    override def saveRepository(repo: Repository): F[Unit] = for {
      _ <- repository.createUser(repo.owner)
      _ <- repository.upsertRepository(repo)
    } yield ()

    private def fetchPullRequests(
      user: User,
      repository: Repository,
      pagination: Option[Pagination]
    ): F[List[PullRequest.Refresh]] = for {
      response <- gitHubClient
        .use(
          _.pullRequests
            .listPullRequests(
              user.login.value,
              repository.name.value,
              List(PRFilterAll, PRFilterSortUpdated, PRFilterOrderDesc),
              pagination
            )
        )
      _ <- logger.debug(s"Rate limit headers: ${response.headers.filter(p =>
          Set(
            "retry-after",
            "x-ratelimit-remaining",
            "x-ratelimit-reset",
            "x-ratelimit-resource"
          ).contains(p._1.toLowerCase)
        )}")
      prs <- response.result match {
        case Left(err)    => err.raiseError[F, List[PullRequest.Refresh]]
        case Right(value) => value.map(PullRequest.Refresh(_, SynchronizedAt.now)).pure[F]
      }
    } yield prs

    private def fetchAllPullRequests(
      user: User,
      repo: Repository,
      synchronizedAt: Option[Repository.SynchronizedAt]
    ): F[List[PullRequest.Refresh]] = {
      def inner(
        pagination: Option[Pagination],
        pullRequests: List[PullRequest.Refresh]
      ): F[List[PullRequest.Refresh]] = pagination match {
        case Some(currentPage) =>
          for {
            prs <- fetchPullRequests(user, repo, pagination)
            _   <- logger.debug(s"Page ${pagination.map(_.page).getOrElse(-1)} returned ${prs.size} pull requests")
            nextPage = getNextPage(currentPage, synchronizedAt, prs)
            result <- inner(nextPage, pullRequests ++ prs)
          } yield result
        case None => pullRequests.pure[F]
      }
      inner(Option(FIRST_PAGE), List.empty)
    }

    // TODO add semaphore for avoiding concurrent runs
    override def synchronizePullRequests: F[Unit] =
      repository
        .findAllRepositories()
        .evalMap { case repo @ Repository(_, owner, name, synchronizedAt) =>
          for {
            now      <- Instant.now().pure[F]
            prs      <- fetchAllPullRequests(owner, repo, synchronizedAt)
            newUsers <- repository.createUsers(prs.map(_.user).distinct)
            _        <- logger.debug(s"Created $newUsers new users")
            saved    <- repository.upsertPullRequestsRefresh(prs)
            _        <- logger.debug(s"Fetched ${prs.size} prs for ${owner.login.value}/${name.value}; saved $saved")
            updatedRepo = repo.copy(synchronizedAt = Option(Repository.SynchronizedAt(now)))
            _ <- repository.upsertRepository(updatedRepo)
          } yield (updatedRepo, saved)
        }
        .compile
        .toList
        .void
  }

  object Impl {

    def apply[F[_]: LoggerFactory: Async](config: GitHub, repository: GitHubRepository[F]): GitHubService[F] =
      new Impl(config, repository)
  }

}
