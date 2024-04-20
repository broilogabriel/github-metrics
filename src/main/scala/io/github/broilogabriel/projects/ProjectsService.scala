package io.github.broilogabriel.projects

import cats._
import cats.implicits._
import io.github.broilogabriel.projects.model.{Metrics, ProjectId}
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

sealed trait ProjectsService[F[_]] {
  def getMetrics(projectId: ProjectId): F[Metrics]
}

object ProjectsService {

  final class Impl[F[_]: LoggerFactory: Monad] extends ProjectsService[F] {
    val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    override def getMetrics(projectId: ProjectId): F[Metrics] = for {
      _       <- logger.info(s"Got a request for project id $projectId")
      metrics <- Metrics.Empty.pure[F]
    } yield metrics
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Monad](): ProjectsService[F] = new Impl()
  }

}
