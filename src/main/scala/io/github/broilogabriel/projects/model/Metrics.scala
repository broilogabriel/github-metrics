package io.github.broilogabriel.projects.model

import io.circe._
import io.circe.generic.semiauto._

final case class Metrics(
  totalContributors: TotalContributors,
  totalCommits: TotalCommits,
  totalClosedPRs: TotalClosedPRs,
  totalOpenPRs: TotalOpenPRs
)

object Metrics {

  implicit val encoder: Encoder[Metrics] = deriveEncoder[Metrics]
  implicit val decoder: Decoder[Metrics] = deriveDecoder[Metrics]

  val Empty: Metrics = Metrics(TotalContributors(0), TotalCommits(0), TotalClosedPRs(0), TotalOpenPRs(0))

}
