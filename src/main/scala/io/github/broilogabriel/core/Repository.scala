package io.github.broilogabriel.core

import cats.effect.MonadCancelThrow
import doobie.hikari.HikariTransactor
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.github.GitHubRepository

private[core] class Repository[F[_]: LoggerFactory: MonadCancelThrow](xa: HikariTransactor[F]) {
  val gitHubRepository: GitHubRepository[F] = GitHubRepository.Impl(xa)
}

object Repository {
  def apply[F[_]: LoggerFactory: MonadCancelThrow](database: HikariTransactor[F]): Repository[F] =
    new Repository(database)

}
