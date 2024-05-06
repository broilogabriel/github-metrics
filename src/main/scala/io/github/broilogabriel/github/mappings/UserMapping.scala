package io.github.broilogabriel.github.mappings

import doobie.postgres.implicits._
import doobie.util.{Read, Write}

import io.github.broilogabriel.github.model.User
import io.github.broilogabriel.github.model.User.{Id, Login, Type}

trait UserMapping {

  implicit val userWrite: Write[User] = Write[(Id, Login, Type)].contramap(u => (u.id, u.login, u.`type`))
  implicit val userRead: Read[User] = Read[(Long, String, String)].map { case (id, login, _type) =>
    User(Id(id), Login(login), Type(_type))
  }

}
