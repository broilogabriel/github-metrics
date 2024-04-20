package io.github.broilogabriel.projects.model

import io.circe.{Decoder, Encoder}

final case class ProjectId(value: Long) extends AnyVal

object ProjectId {

  implicit val encoder: Encoder[ProjectId] = Encoder.encodeLong.contramap(_.value)
  implicit val decoder: Decoder[ProjectId] = Decoder.decodeLong.map(ProjectId.apply)

}
