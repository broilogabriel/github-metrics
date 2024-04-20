package io.github.broilogabriel.projects.model

import io.circe.{Decoder, Encoder}

final case class TotalClosedPRs(value: Long) extends AnyVal

object TotalClosedPRs {

  implicit val encoder: Encoder[TotalClosedPRs] = Encoder.encodeLong.contramap(_.value)
  implicit val decoder: Decoder[TotalClosedPRs] = Decoder.decodeLong.map(TotalClosedPRs.apply)

}
