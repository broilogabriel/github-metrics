package io.github.broilogabriel.core

import cats.Monad
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.github.GitHubService
import io.github.broilogabriel.projects.ProjectsService

private[core] class Service[F[_]: LoggerFactory: Monad](repository: Repository[F]) {

  val gitHubService: GitHubService[F]     = GitHubService.Impl(repository.gitHubRepository)
  val projectsService: ProjectsService[F] = ProjectsService.Impl()

}

object Service {
  def apply[F[_]: LoggerFactory: Monad](repository: Repository[F]): Service[F] = new Service(repository)
}
