package io.github.broilogabriel.core

import io.github.broilogabriel.core.Config.GitHub

final case class Config(
  github: GitHub
//  database: hikari.Config
)

object Config {
  final case class GitHub(token: String, owner: String, repo: String, cron: String)
}
