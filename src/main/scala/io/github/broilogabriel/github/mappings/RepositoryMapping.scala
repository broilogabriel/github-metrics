package io.github.broilogabriel.github.mappings

import java.time.Instant

import doobie.postgres.circe.jsonb.implicits._
import doobie.postgres.implicits._
import doobie.util.{Read, Write}

import io.github.broilogabriel.github.model.{Repository, User}
import io.github.broilogabriel.github.model.Repository.{Id, Name, SynchronizedAt}
import io.github.broilogabriel.github.model.User.{Login, Type}

trait RepositoryMapping {

  implicit val repositoryWrite: Write[Repository] =
    Write[(Long, Long, String, Option[Instant])].contramap(r =>
      (r.id.value, r.owner.id.value, r.name.value, r.synchronizedAt.map(_.value))
    )
  implicit val repositoryRead: Read[Repository] = Read[(Long, String, Long, String, String, Option[Instant])].map {
    case (id, name, ownerId, login, _type, sync) =>
      Repository(
        Id(id),
        User(User.Id(ownerId), Login(login), Type(_type)),
        Name(name),
        sync.map(SynchronizedAt)
      )
  }

}
