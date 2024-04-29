package io.github.broilogabriel.projects

import cats._
import cats.effect.Async
import cats.implicits._
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.core.Config.GitHub
import io.github.broilogabriel.github.{GitHubRepository, GitHubService}
import io.github.broilogabriel.projects.model.{Metrics, ProjectId}

sealed trait ProjectsService[F[_]] {
  def getMetrics(projectId: ProjectId): F[Metrics]
}

object ProjectsService {

  private final class Impl[F[_]: LoggerFactory: Async](repository: ProjectsRepository[F]) extends ProjectsService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    override def getMetrics(projectId: ProjectId): F[Metrics] = for {
      _       <- logger.info(s"Got a request for project id $projectId")
      metrics <- repository.readMetrics(projectId)
    } yield metrics
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Async](repository: ProjectsRepository[F]): ProjectsService[F] =
      new Impl(repository)
  }

}
