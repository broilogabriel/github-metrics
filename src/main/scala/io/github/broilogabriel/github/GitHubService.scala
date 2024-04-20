package io.github.broilogabriel.github

import cats._
import cats.implicits._
import io.circe.Json
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

sealed trait GitHubService[F[_]] {
  def saveNotification(notification: Json): F[Unit]
}

object GitHubService {

  private final class Impl[F[_]: LoggerFactory: Monad](repository: GitHubRepository[F]) extends GitHubService[F] {
    private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
    override def saveNotification(notification: Json): F[Unit] = for {
      _ <- logger.info(s"Got notification: $notification")
    } yield ()
  }

  object Impl {
    def apply[F[_]: LoggerFactory: Monad](repository: GitHubRepository[F]): GitHubService[F] = new Impl(repository)
  }

}
