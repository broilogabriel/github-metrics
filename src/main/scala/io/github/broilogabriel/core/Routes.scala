package io.github.broilogabriel.core

import cats.ApplicativeThrow
import cats.data.{Kleisli, OptionT}
import cats.effect.Concurrent
import cats.implicits._
import org.http4s.{Request, Response}
import org.http4s.server.Router
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

import io.github.broilogabriel.contributors.ContributorsRoutes
import io.github.broilogabriel.github.GitHubRoutes
import io.github.broilogabriel.projects.ProjectsRoutes

final class Routes[F[_]: LoggerFactory: ApplicativeThrow: Concurrent] private[core] (service: Service[F]) {
  private val logger: SelfAwareStructuredLogger[F]      = LoggerFactory[F].getLogger
  private val gitHubRoutes: GitHubRoutes[F]             = GitHubRoutes[F](service.gitHubService)
  private val projectsRoutes: ProjectsRoutes[F]         = ProjectsRoutes[F](service.projectsService)
  private val contributorsRoutes: ContributorsRoutes[F] = ContributorsRoutes[F](service.contributorsService)

  private def errorHandler(t: Throwable, msg: => String) = OptionT.liftF(
    logger.error(t)(msg)
  )

  private val withErrorLogging = ErrorHandling.Recover.total(
    ErrorAction.log(
      gitHubRoutes.routes <+> projectsRoutes.routes <+> contributorsRoutes.routes,
      messageFailureLogAction = errorHandler,
      serviceErrorLogAction = errorHandler
    )
  )

  val endpoints: Kleisli[F, Request[F], Response[F]] = Router("/api" -> withErrorLogging).orNotFound
}
object Routes {
  def apply[F[_]: LoggerFactory: Concurrent](service: Service[F]): Routes[F] = new Routes(service)
}
