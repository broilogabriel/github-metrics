package io.github.broilogabriel.github

import java.time.Instant

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.circe.jsonb.implicits._
import doobie.postgres.implicits._
import io.circe._
import io.circe.syntax._
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.github.mappings.{PullRequestMapping, RepositoryMapping, UserMapping}
import io.github.broilogabriel.github.model.{DeliveryId, PullRequest, PullRequestEvent, Repository, User}

trait GitHubRepository[F[_]] {

  def createNotification(deliveryId: DeliveryId, pr: PullRequestEvent): F[Unit]
  def findAllRepositories(): fs2.Stream[F, Repository]
  def createUser(user: User): F[Unit]
  def createUsers(users: List[User]): F[Int]
  def upsertRepository(repository: Repository): F[Int]
  def upsertPullRequestsRefresh(pullRequests: List[PullRequest.Refresh]): F[Int]
  def upsertPullRequestWebhook(pullRequest: PullRequest.WebHook): F[Int]

}
object GitHubRepository {
  private final class Impl[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F])
      extends GitHubRepository[F]
      with UserMapping
      with RepositoryMapping
      with PullRequestMapping {

    override def createNotification(deliveryId: DeliveryId, pr: model.PullRequestEvent): F[Unit] =
      sql"""INSERT INTO github_events (id, data) VALUES (${deliveryId.value}, ${pr.asJson}) ON CONFLICT DO NOTHING""".update.run
        .transact(xa)
        .void

    def createUser(user: User): F[Unit] =
      sql"""INSERT INTO users (id, login, type) VALUES ($user) ON CONFLICT DO NOTHING""".update.run.transact(xa).void

    def createUsers(users: List[User]): F[Int] = {
      val sql = """INSERT INTO users (id, login, type) VALUES (?,?,?) ON CONFLICT DO NOTHING"""
      Update[User](sql)
        .updateMany(users)
        .transact(xa)
    }
    override def upsertRepository(repository: Repository): F[Int] =
      sql"""INSERT INTO repositories (id, owner_id, name, synchronized_at) VALUES ($repository)
        |    ON CONFLICT (id) DO UPDATE
        |       SET synchronized_at = EXCLUDED.synchronized_at
        |""".stripMargin.update.run
        .transact(xa)

    def upsertPullRequestWebhook(pullRequest: PullRequest.WebHook): F[Int] =
      sql"""INSERT INTO pull_requests (
                  |  id,
                  |  repository_id,
                  |  user_id,
                  |  merge_commit_sha,
                  |  state,
                  |  created_at,
                  |  updated_at,
                  |  closed_at,
                  |  merged_at,
                  |  webhook_event_at
                  |) VALUES ($pullRequest)
                  |    ON CONFLICT (id) DO UPDATE
                  |       SET updated_at       = EXCLUDED.updated_at,
                  |           closed_at        = EXCLUDED.closed_at,
                  |           merged_at        = EXCLUDED.merged_at,
                  |           webhook_event_at = EXCLUDED.webhook_event_at
                  |     WHERE pull_requests.updated_at < EXCLUDED.updated_at
                  |       """.stripMargin.update.run.transact(xa)

    override def upsertPullRequestsRefresh(pullRequests: List[PullRequest.Refresh]): F[Int] = {
      val sql = """INSERT INTO pull_requests (
           |  id,
           |  repository_id,
           |  user_id,
           |  merge_commit_sha,
           |  state,
           |  created_at,
           |  updated_at,
           |  closed_at,
           |  merged_at,
           |  synchronized_at
           |) VALUES (?,?,?,?,?,?,?,?,?,?)
           |    ON CONFLICT (id) DO UPDATE
           |       SET updated_at      = EXCLUDED.updated_at,
           |           closed_at       = EXCLUDED.closed_at,
           |           merged_at       = EXCLUDED.merged_at,
           |           synchronized_at = EXCLUDED.synchronized_at
           |     WHERE pull_requests.updated_at      < EXCLUDED.updated_at
           |       """.stripMargin
      Update[PullRequest.Refresh](sql)
        .updateMany(pullRequests)
        .transact(xa)
    }

    def findAllRepositories(): fs2.Stream[F, Repository] = sql"""
            SELECT r.id
                 , r.name
                 , r.owner_id
                 , u.login
                 , u.type
                 , r.synchronized_at
              FROM repositories r
              JOIN users        u ON u.id = r.owner_id
                 """.query[Repository].stream.transact(xa)
  }

  object Impl {
    def apply[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]): GitHubRepository[F] = new Impl(xa)
  }
}
