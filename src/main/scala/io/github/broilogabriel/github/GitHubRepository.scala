package io.github.broilogabriel.github

import cats.effect._
import cats.effect.MonadCancelThrow
import cats.syntax.all._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres.circe.jsonb.implicits._
import doobie.postgres.implicits._
import io.circe.Json
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.github.model.DeliveryId

trait GitHubRepository[F[_]] {

  def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit]

}
object GitHubRepository {
  private final class Impl[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]) extends GitHubRepository[F] {
    override def saveNotification(deliveryId: DeliveryId, data: Json): F[Unit] =
      sql"""INSERT INTO github_events (id, data) VALUES (${deliveryId.value}, $data) ON CONFLICT DO NOTHING""".update.run
        .transact(xa)
        .void
  }

  object Impl {
    def apply[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]): GitHubRepository[F] = new Impl(xa)
  }
}
