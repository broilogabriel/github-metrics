package io.github.broilogabriel.github

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
import io.github.broilogabriel.github.model.{DeliveryId, Repository, User}

trait GitHubRepository[F[_]] {

  def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit]
  def findAllRepositories(): fs2.Stream[F, Repository]
  def createUser(user: User): F[Unit]
  def createRepository(repository: Repository): F[Unit]

}
object GitHubRepository {
  private final class Impl[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F])
      extends GitHubRepository[F]
      with UserMapping
      with RepositoryMapping {
    override def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit] =
      sql"""INSERT INTO github_events (id, data) VALUES (${deliveryId.value}, $data) ON CONFLICT DO NOTHING""".update.run
        .transact(xa)
        .void

    def createUser(user: User): F[Unit] =
      sql"""INSERT INTO users (id, login, type) VALUES ($user) ON CONFLICT DO NOTHING""".update.run.transact(xa).void
    def createRepository(repository: Repository): F[Unit] =
      sql"""INSERT INTO repositories (id, owner_id, name) VALUES ($repository) ON CONFLICT DO NOTHING""".update.run
        .transact(xa)
        .void
    def findAllRepositories(): fs2.Stream[F, Repository] = sql"""
            SELECT r.id
                 , r.name
                 , r.owner_id
                 , u.login
                 , u.type
              FROM repositories r
              JOIN users        u ON u.id = r.owner_id
                 """.query[Repository].stream.transact(xa)
  }

  object Impl {
    def apply[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]): GitHubRepository[F] = new Impl(xa)
  }
}
