package io.github.broilogabriel.contributors

import scala.util.Try

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

import io.github.broilogabriel.contributors.ContributorsRoutes.ContributorIdVar
import io.github.broilogabriel.contributors.model.ContributorId

final class ContributorsRoutes[F[_]: Monad](service: ContributorsService[F]) extends Http4sDsl[F] {

  private val metrics = HttpRoutes.of[F] { case GET -> Root / ContributorIdVar(contributorId) / "metrics" =>
    Ok(service.getMetrics(contributorId))
  }

  val routes: HttpRoutes[F] = Router(
    ("/contributors", metrics)
  )

}

object ContributorsRoutes {
  final object ContributorIdVar {
    def unapply(str: String): Option[ContributorId] = Try(ContributorId(str.toLong)).toOption
  }
  def apply[F[_]: Monad](service: ContributorsService[F]): ContributorsRoutes[F] = new ContributorsRoutes(service)
}
