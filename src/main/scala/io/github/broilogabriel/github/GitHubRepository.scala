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
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.github.mappings.{RepositoryMapping, UserMapping}
import io.github.broilogabriel.github.model.{DeliveryId, PullRequest, Repository, User}

trait GitHubRepository[F[_]] {

  def createNotification(deliveryId: DeliveryId, data: Json): F[Unit]
  def findAllRepositories(): fs2.Stream[F, Repository]
  def createUser(user: User): F[Unit]
  def createUsers(users: List[User]): F[Int]
  def upsertRepository(repository: Repository): F[Unit]
  def createPullRequests(pullRequests: List[PullRequest]): F[Int]

}
object GitHubRepository {
  private final class Impl[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F])
      extends GitHubRepository[F]
      with UserMapping
      with RepositoryMapping {

    override def createNotification(deliveryId: DeliveryId, data: Json): F[Unit] =
      sql"""INSERT INTO github_events (id, data) VALUES (${deliveryId.value}, $data) ON CONFLICT DO NOTHING""".update.run
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
    def upsertRepository(repository: Repository): F[Unit] =
      sql"""INSERT INTO repositories (id, owner_id, name, synchronized_at) VALUES ($repository)
        |    ON CONFLICT (id) DO UPDATE
        |       SET synchronized_at = EXCLUDED.synchronized_at
        |""".stripMargin.update.run
        .transact(xa)
        .void

    def createPullRequests(pullRequests: List[PullRequest]): F[Int] = {
      type UpdatePullRequest =
        (
          PullRequest.Id,
          Long,
          Long,
          Option[String],
          String,
          Instant,
          Option[Instant],
          Option[Instant],
          Option[Instant],
          Option[Instant]
        )
      val prs: Seq[UpdatePullRequest] = pullRequests.map {
        case PullRequest(
              id,
              repository,
              user,
              mergeCommitSha,
              state,
              createdAt,
              updatedAt,
              closedAt,
              mergedAt,
              synchronizedAt
            ) =>
          (
            id,
            repository.id.value,
            user.id.value,
            mergeCommitSha.map(_.value),
            state.value,
            createdAt.value,
            updatedAt.map(_.value),
            closedAt.map(_.value),
            mergedAt.map(_.value),
            synchronizedAt.map(_.value)
          )
      }
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
           |       """.stripMargin
      Update[UpdatePullRequest](sql)
        .updateMany(prs)
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
