package io.github.broilogabriel.core

import doobie.hikari

import io.github.broilogabriel.core.Config.GitHub

final case class Config(
  github: GitHub
//  database: hikari.Config
)

object Config {
  final case class GitHub(token: String)
}
