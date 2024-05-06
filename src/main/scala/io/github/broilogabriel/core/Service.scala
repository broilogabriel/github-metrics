package io.github.broilogabriel.core

import cats.effect.Async
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.contributors.ContributorsService
import io.github.broilogabriel.github.GitHubService
import io.github.broilogabriel.projects.ProjectsService

/**
 * Responsible for creating the service layer of the application and injecting dependencies
 * @param config Configuration containing settings needed by the application
 * @param repository Repository layer to be used by services
 * @tparam F
 */
final class Service[F[_]: LoggerFactory: Async] private[core] (config: Config, repository: Repository[F]) {

  val gitHubService: GitHubService[F]             = GitHubService.Impl(config.github, repository.gitHubRepository)
  val projectsService: ProjectsService[F]         = ProjectsService.Impl(repository.projectsRepository)
  val contributorsService: ContributorsService[F] = ContributorsService.Impl(repository.contributorsRepository)

}

object Service {
  def apply[F[_]: LoggerFactory: Async](config: Config, repository: Repository[F]): Service[F] =
    new Service(config, repository)
}
