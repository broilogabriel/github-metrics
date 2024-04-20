package io.github.broilogabriel.core

import cats.effect.{Async, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor

object Database {

  def apply[F[_]: Async](
    host: String,
    port: Int,
    dbName: String,
    user: String,
    pass: String,
    schema: String
  ): Resource[F, HikariTransactor[F]] =
    for {
      hikariConfig <- Resource.pure {
        val config = new HikariConfig()
        config.setDriverClassName("org.postgresql.Driver")
        config.setJdbcUrl(s"jdbc:postgresql://$host:$port/$dbName")
        config.setUsername(user)
        config.setPassword(pass)
        config.setSchema(schema)
        config.setMaximumPoolSize(32)
        config
      }
      xa <- HikariTransactor.fromHikariConfig[F](hikariConfig)
    } yield xa

}
