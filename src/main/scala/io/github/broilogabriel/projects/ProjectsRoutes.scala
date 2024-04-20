package io.github.broilogabriel.projects

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class ProjectsRoutes[F[_]: Monad](service: ProjectsService[F]) extends Http4sDsl[F] {

  private val metrics = HttpRoutes.of[F] { case GET -> Root / ProjectIdVar(projectId) / "metrics" =>
    Ok(service.getMetrics(projectId))
  }

  val routes: HttpRoutes[F] = Router(
    ("/projects", metrics)
  )

}

object ProjectsRoutes {
  def apply[F[_]: Monad](service: ProjectsService[F]): ProjectsRoutes[F] = new ProjectsRoutes(service)
}
