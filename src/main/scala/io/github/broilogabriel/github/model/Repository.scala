package io.github.broilogabriel.github.model

import io.circe.Codec
import io.circe.generic.semiauto

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.Repository.{Id, Name}

final case class Repository(id: Id, owner: User, name: Name)

object Repository extends ValueClassCodec {
  final case class Id(value: Long)     extends AnyVal
  final case class Name(value: String) extends AnyVal

  implicit val repositoryCodec: Codec[Repository] = semiauto.deriveCodec[Repository]
}
