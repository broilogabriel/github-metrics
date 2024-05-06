package io.github.broilogabriel.core

import cats.effect.{Async, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor

object Database {

  /**
   * Utility object for creating the database connection from a config
   */
  def apply[F[_]: Async](databaseSettings: Config.DatabaseSettings): Resource[F, HikariTransactor[F]] =
    databaseSettings match {
      case Config.DatabaseSettings(host, port, name, user, pass, schema) =>
        for {
          hikariConfig <- Resource.pure {
            val config = new HikariConfig()
            config.setDriverClassName("org.postgresql.Driver")
            config.setJdbcUrl(s"jdbc:postgresql://$host:$port/$name")
            config.setUsername(user)
            config.setPassword(pass)
            config.setSchema(schema)
            config.setMaximumPoolSize(32)
            config
          }
          xa <- HikariTransactor.fromHikariConfig[F](hikariConfig)
        } yield xa
    }

}
