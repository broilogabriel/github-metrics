package io.github.broilogabriel.contributors.model

import io.circe._
import io.circe.generic.semiauto._

import io.github.broilogabriel.contributors.model.Metrics.{
  TotalClosedPullRequests,
  TotalCommits,
  TotalOpenPullRequests,
  TotalProjects
}
import io.github.broilogabriel.core.ValueClassCodec

final case class Metrics(
  totalContributors: TotalProjects,
  totalCommits: TotalCommits,
  totalClosedPullRequests: TotalClosedPullRequests,
  totalOpenPullRequests: TotalOpenPullRequests
)

object Metrics extends ValueClassCodec {

  final case class TotalProjects(value: Long)           extends AnyVal
  final case class TotalCommits(value: Long)            extends AnyVal
  final case class TotalClosedPullRequests(value: Long) extends AnyVal
  final case class TotalOpenPullRequests(value: Long)   extends AnyVal

  implicit val encoder: Encoder[Metrics] = deriveEncoder[Metrics]
  implicit val decoder: Decoder[Metrics] = deriveDecoder[Metrics]

  val Empty: Metrics =
    Metrics(TotalProjects(0), TotalCommits(0), TotalClosedPullRequests(0), TotalOpenPullRequests(0))

}
