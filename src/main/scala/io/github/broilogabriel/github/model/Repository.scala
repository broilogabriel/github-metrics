package io.github.broilogabriel.github.model

import java.time.Instant

import io.circe.Codec
import io.circe.generic.semiauto

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.Repository.{Id, Name, SynchronizedAt}

final case class Repository(id: Id, owner: User, name: Name, synchronizedAt: Option[SynchronizedAt])

object Repository extends ValueClassCodec {
  final case class Id(value: Long)                extends AnyVal
  final case class Name(value: String)            extends AnyVal
  final case class SynchronizedAt(value: Instant) extends AnyVal
  object SynchronizedAt {
    def now: SynchronizedAt = SynchronizedAt(Instant.now())
  }
  def apply(repo: github4s.domain.Repository, synchronizedAt: Option[SynchronizedAt]) = new Repository(
    Id(repo.id),
    User(repo.owner),
    Name(repo.name),
    synchronizedAt
  )

  implicit val repositoryCodec: Codec[Repository] = semiauto.deriveCodec[Repository]
}
