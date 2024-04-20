package io.github.broilogabriel.core

import cats.effect.{Async, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {

  def apply[F[_]: Async](dbName: String, user: String, pass: String): Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool(32)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      s"jdbc:postgresql:$dbName",
      user,
      pass,
      ec
    )
  } yield xa

}
