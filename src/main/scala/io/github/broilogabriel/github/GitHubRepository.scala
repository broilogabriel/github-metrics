package io.github.broilogabriel.github

import doobie.hikari.HikariTransactor
import io.circe.Json
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

trait GitHubRepository[F[_]] {

  def saveNotification(json: Json): F[Unit]

}
object GitHubRepository {
  private final class Impl[F[_]: LoggerFactory](xa: HikariTransactor[F]) extends GitHubRepository[F] {
    private val logger: SelfAwareStructuredLogger[F]   = LoggerFactory[F].getLogger
    override def saveNotification(json: Json): F[Unit] = ???
  }

  object Impl {
    def apply[F[_]: LoggerFactory](xa: HikariTransactor[F]): GitHubRepository[F] = new Impl(xa)
  }
}
