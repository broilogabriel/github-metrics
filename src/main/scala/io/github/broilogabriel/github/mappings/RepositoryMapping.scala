package io.github.broilogabriel.github.mappings

import doobie.util.{Read, Write}

import io.github.broilogabriel.github.model.{Repository, User}
import io.github.broilogabriel.github.model.Repository.{Id, Name}
import io.github.broilogabriel.github.model.User.{Login, Type}

trait RepositoryMapping {

  implicit val repositoryWrite: Write[Repository] =
    Write[(Long, Long, String)].contramap(r => (r.id.value, r.owner.id.value, r.name.value))
  implicit val repositoryRead: Read[Repository] = Read[(Long, String, Long, String, String)].map {
    case (id, name, ownerId, login, _type) =>
      Repository(Id(id), User(User.Id(ownerId), Login(login), Type(_type)), Name(name))
  }

}
