package io.github.broilogabriel.github.model

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.User.{Id, Login, Type}

final case class User(id: Id, login: Login, `type`: Type)

object User extends ValueClassCodec {
  final case class Id(value: Long)      extends AnyVal
  final case class Login(value: String) extends AnyVal
  final case class Type(value: String)  extends AnyVal

//  implicit val repoIdCodec: Codec[Id] =
//    Codec.from(Decoder.decodeLong.map(Id.apply), Encoder.encodeLong.contramap(_.value))
//  implicit val repoNameCodec: Codec[Name] =
//    Codec.from(Decoder.decodeString.map(Name.apply), Encoder.encodeString.contramap(_.value))
  implicit val userCodec: Codec[User] = semiauto.deriveCodec[User]
}
