package io.github.broilogabriel.core

import io.github.broilogabriel.core.Config.GitHub

final case class Config(
  github: GitHub
)

object Config {
  final case class GitHub(token: String, cron: String)
}
