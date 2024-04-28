package io.github.broilogabriel.projects

import cats._
import cats.implicits._
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.projects.model.{Metrics, ProjectId}

sealed trait ProjectsService[F[_]] {
  def getMetrics(projectId: ProjectId): F[Metrics]
}

object ProjectsService {

  private final class Impl[F[_]: LoggerFactory: Monad] extends ProjectsService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    override def getMetrics(projectId: ProjectId): F[Metrics] = for {
      _       <- logger.info(s"Got a request for project id $projectId")
      metrics <- Metrics.Empty.pure[F]
    } yield metrics
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Monad](): ProjectsService[F] = new Impl()
  }

  //  select r."name"
  // , count(distinct pr.user_id) total_contributors
  // , count(*) filter (where merged_at is not null) total_commits
  // , count(*) filter (where closed_at is null and merged_at is null) total_open_pull_requests
  // , count(*) filter (where closed_at is not null and merged_at is null) total_closed_pull_requests
  // from pull_requests pr
  // join repositories r on r.id = pr.repository_id
  // where r.name = 'cats'
  // group by r."name";
  //
  //
  // select u."login"
  // , count(distinct pr.repository_id) total_projects
  // , count(*) filter (where merged_at is not null) total_commits
  // , count(*) filter (where closed_at is null and merged_at is null) total_open_pull_requests
  // , count(*) filter (where closed_at is not null and merged_at is null) total_closed_pull_requests
  // from pull_requests pr
  // join users u on u.id = pr.user_id
  // where u.login = 'amir'
  // group by u."login";

}
