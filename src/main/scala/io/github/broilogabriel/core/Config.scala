package io.github.broilogabriel.core

import io.github.broilogabriel.core.Config.{DatabaseSettings, GitHub}

final case class Config(
  github: GitHub,
  db: DatabaseSettings
)

object Config {
  final case class GitHub(token: String, cron: String)
  final case class DatabaseSettings(host: String, port: Int, name: String, user: String, pass: String, schema: String)
}
