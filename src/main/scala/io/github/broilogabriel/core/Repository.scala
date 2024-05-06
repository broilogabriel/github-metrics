package io.github.broilogabriel.core

import cats.effect.MonadCancelThrow
import doobie.hikari.HikariTransactor
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.contributors.ContributorsRepository
import io.github.broilogabriel.github.GitHubRepository
import io.github.broilogabriel.projects.ProjectsRepository

final class Repository[F[_]: LoggerFactory: MonadCancelThrow] private[core] (xa: HikariTransactor[F]) {
  val gitHubRepository: GitHubRepository[F]             = GitHubRepository.Impl(xa)
  val projectsRepository: ProjectsRepository[F]         = ProjectsRepository.Impl(xa)
  val contributorsRepository: ContributorsRepository[F] = ContributorsRepository.Impl(xa)
}

object Repository {
  def apply[F[_]: LoggerFactory: MonadCancelThrow](database: HikariTransactor[F]): Repository[F] =
    new Repository(database)

}
