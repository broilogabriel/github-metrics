package io.github.broilogabriel.projects.model

import io.circe._
import io.circe.generic.semiauto._

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.projects.model.Metrics.{
  TotalClosedPullRequests,
  TotalCommits,
  TotalContributors,
  TotalOpenPullRequests
}

final case class Metrics(
  totalContributors: TotalContributors,
  totalCommits: TotalCommits,
  totalClosedPullRequests: TotalClosedPullRequests,
  totalOpenPullRequests: TotalOpenPullRequests
)

object Metrics extends ValueClassCodec {

  final case class TotalContributors(value: Long)       extends AnyVal
  final case class TotalCommits(value: Long)            extends AnyVal
  final case class TotalClosedPullRequests(value: Long) extends AnyVal
  final case class TotalOpenPullRequests(value: Long)   extends AnyVal

  implicit val encoder: Encoder[Metrics] = deriveEncoder[Metrics]
  implicit val decoder: Decoder[Metrics] = deriveDecoder[Metrics]

  val Empty: Metrics =
    Metrics(TotalContributors(0), TotalCommits(0), TotalClosedPullRequests(0), TotalOpenPullRequests(0))

}
