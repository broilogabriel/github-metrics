package io.github.broilogabriel.github.headers

import org.http4s.{Header, ParseResult}
import org.typelevel.ci.CIStringSyntax

final case class `X-GitHub-Event`(value: String) extends AnyVal {
  def to[T](f: String => T): T = f(value)
}

object `X-GitHub-Event` {

  val PULL_REQUEST: `X-GitHub-Event` = `X-GitHub-Event`("pull_request")

  private def parse(s: String): ParseResult[`X-GitHub-Event`] = if (s.nonEmpty) {
    ParseResult.success(`X-GitHub-Event`(s))
  } else {
    ParseResult.fail(s"Failed to parse", "X-GitHub-Event is empty")
  }

  implicit val headerInstance: Header[`X-GitHub-Event`, Header.Single] =
    Header.createRendered(
      ci"X-GitHub-Event",
      _.value,
      parse
    )

}
