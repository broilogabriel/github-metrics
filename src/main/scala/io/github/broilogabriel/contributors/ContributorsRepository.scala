package io.github.broilogabriel.contributors

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
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.contributors.model.{ContributorId, Metrics}

trait ContributorsRepository[F[_]] {
  def readMetrics(contributorId: ContributorId): F[Metrics]
}

object ContributorsRepository {

  private final class Impl[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F])
      extends ContributorsRepository[F] {
    override def readMetrics(contributorId: ContributorId): F[Metrics] = sql"""
            |SELECT total_projects
            |     , total_commits
            |     , total_open_pull_requests
            |     , total_closed_pull_requests
            |  FROM contributors_metrics
            | WHERE id = $contributorId
            |""".stripMargin.query[Metrics].unique.transact(xa)
  }

  object Impl {
    def apply[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]): ContributorsRepository[F] = new Impl(xa)
  }
}
