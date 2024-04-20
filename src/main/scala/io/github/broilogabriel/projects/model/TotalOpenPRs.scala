package io.github.broilogabriel.projects.model

import io.circe.{Decoder, Encoder}

final case class TotalOpenPRs(value: Long) extends AnyVal

object TotalOpenPRs {

  implicit val encoder: Encoder[TotalOpenPRs] = Encoder.encodeLong.contramap(_.value)
  implicit val decoder: Decoder[TotalOpenPRs] = Decoder.decodeLong.map(TotalOpenPRs.apply)

}
