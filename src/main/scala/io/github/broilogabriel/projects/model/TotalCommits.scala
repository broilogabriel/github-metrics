package io.github.broilogabriel.projects.model

import io.circe.{Decoder, Encoder}

final case class TotalCommits(value: Long) extends AnyVal

object TotalCommits {

  implicit val encoder: Encoder[TotalCommits] = Encoder.encodeLong.contramap(_.value)
  implicit val decoder: Decoder[TotalCommits] = Decoder.decodeLong.map(TotalCommits.apply)

}
