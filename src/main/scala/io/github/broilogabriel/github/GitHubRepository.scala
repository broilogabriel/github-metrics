package io.github.broilogabriel.github

import io.circe.Json
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

trait GitHubRepository[F[_]] {

  def saveNotification(json: Json): F[Unit]

}
object GitHubRepository {
  private final class Impl[F[_]: LoggerFactory] extends GitHubRepository[F] {
    private val logger: SelfAwareStructuredLogger[F]   = LoggerFactory[F].getLogger
    override def saveNotification(json: Json): F[Unit] = ???
  }

  object Impl {
    def apply[F[_]: LoggerFactory]: GitHubRepository[F] = new Impl()
  }
}
