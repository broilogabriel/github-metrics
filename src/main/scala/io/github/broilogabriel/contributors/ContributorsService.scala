package io.github.broilogabriel.contributors

import cats._
import cats.effect.Async
import cats.implicits._
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.contributors.model.{ContributorId, Metrics}

sealed trait ContributorsService[F[_]] {
  def getMetrics(contributorId: ContributorId): F[Metrics]
}

object ContributorsService {

  private final class Impl[F[_]: LoggerFactory: Async](repository: ContributorsRepository[F])
      extends ContributorsService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    override def getMetrics(contributorId: ContributorId): F[Metrics] = for {
      _       <- logger.debug(s"Got a request for contributor id $contributorId")
      metrics <- repository.readMetrics(contributorId)
    } yield metrics
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Async](repository: ContributorsRepository[F]): ContributorsService[F] =
      new Impl(repository)
  }

}
