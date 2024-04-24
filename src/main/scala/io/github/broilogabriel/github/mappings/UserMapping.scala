package io.github.broilogabriel.github.mappings

import doobie.util.{Read, Write}

import io.github.broilogabriel.github.model.User
import io.github.broilogabriel.github.model.User._

trait UserMapping {

  implicit val userWrite: Write[User] =
    Write[(Long, String, String)].contramap(u => (u.id.value, u.login.value, u.`type`.value))
  implicit val userRead: Read[User] = Read[(Long, String, String)].map { case (id, login, _type) =>
    User(Id(id), Login(login), Type(_type))
  }

}
