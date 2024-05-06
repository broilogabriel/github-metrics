package io.github.broilogabriel.contributors.model

import io.github.broilogabriel.core.ValueClassCodec

final case class ContributorId(value: Long) extends AnyVal

object ContributorId extends ValueClassCodec
