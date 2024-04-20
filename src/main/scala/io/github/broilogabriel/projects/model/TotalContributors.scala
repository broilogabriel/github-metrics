package io.github.broilogabriel.projects.model

import io.circe.{Decoder, Encoder}

final case class TotalContributors(value: Long) extends AnyVal

object TotalContributors {

  implicit val encoder: Encoder[TotalContributors] = Encoder.encodeLong.contramap(_.value)
  implicit val decoder: Decoder[TotalContributors] = Decoder.decodeLong.map(TotalContributors.apply)

}
