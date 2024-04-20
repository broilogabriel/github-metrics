package io.github.broilogabriel.github.headers

import java.util.UUID

import scala.util.Try

import cats.parse.{Parser, Parser0}
import org.http4s.{Header, ParseResult}
import org.http4s.parser.AdditionalRules
import org.typelevel.ci.{CIString, CIStringSyntax}

// X-GitHub-Delivery: bd34c38e-ff12-11ee-9979-bef41ffdc041
// X-GitHub-Event: push
final case class `X-GitHub-Delivery`(value: UUID)

object `X-GitHub-Delivery` {

  private def parse(s: String): ParseResult[`X-GitHub-Delivery`] =
    Try(UUID.fromString(s)).fold(
      err => ParseResult.fail(s"Failed to parse $s", err.getMessage),
      uuid => ParseResult.success(`X-GitHub-Delivery`(uuid))
    )

  implicit val headerInstance: Header[`X-GitHub-Delivery`, Header.Single] =
    Header.createRendered(
      ci"X-GitHub-Delivery",
      _.value.toString,
      parse
    )

}
