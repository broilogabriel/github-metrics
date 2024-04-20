package io.github.broilogabriel.core

import cats.data.Kleisli
import cats.effect.Concurrent
import cats.implicits._
import org.http4s.{Request, Response}
import org.http4s.server.Router
import org.typelevel.log4cats.LoggerFactory

import io.github.broilogabriel.github.GitHubRoutes
import io.github.broilogabriel.projects.ProjectsRoutes

private[core] class Routes[F[_]: LoggerFactory: Concurrent](service: Service[F]) {
  private val gitHubRoutes: GitHubRoutes[F]     = GitHubRoutes[F](service.gitHubService)
  private val projectsRoutes: ProjectsRoutes[F] = ProjectsRoutes[F](service.projectsService)

  val endpoints: Kleisli[F, Request[F], Response[F]] = Router(
    "/" -> (gitHubRoutes.routes <+> projectsRoutes.routes)
  ).orNotFound
}
object Routes {
  def apply[F[_]: LoggerFactory: Concurrent](service: Service[F]): Routes[F] = new Routes(service)
}
