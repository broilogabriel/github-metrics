package io.github.broilogabriel.github.model

import io.circe.Codec
import io.circe.generic.semiauto

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.User.{Id, Login, Type}

final case class User(id: Id, login: Login, `type`: Type)

object User extends ValueClassCodec {
  final case class Id(value: Long)      extends AnyVal
  final case class Login(value: String) extends AnyVal
  final case class Type(value: String)  extends AnyVal

  def apply(user: github4s.domain.User): User = new User(
    Id(user.id),
    Login(user.login),
    Type(user.`type`)
  )

  implicit val userCodec: Codec[User] = semiauto.deriveCodec[User]
}
