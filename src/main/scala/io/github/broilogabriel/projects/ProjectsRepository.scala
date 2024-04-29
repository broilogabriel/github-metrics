package io.github.broilogabriel.projects

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

import io.github.broilogabriel.projects.model.{Metrics, ProjectId}

trait ProjectsRepository[F[_]] {
  def readMetrics(projectId: ProjectId): F[Metrics]
}

object ProjectsRepository {

  private final class Impl[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F])
      extends ProjectsRepository[F] {
    override def readMetrics(projectId: ProjectId): F[Metrics] = sql"""
            |SELECT total_contributors
            |     , total_commits
            |     , total_open_pull_requests
            |     , total_closed_pull_requests
            |  FROM projects_metrics
            | WHERE id = $projectId
            |""".stripMargin.query[Metrics].unique.transact(xa)
  }

  object Impl {
    def apply[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]): ProjectsRepository[F] = new Impl(xa)
  }
}
