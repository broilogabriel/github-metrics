package io.github.broilogabriel.github.model

import io.circe.{Codec, Decoder, Encoder, HCursor}
import io.circe.generic.semiauto._

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.PullRequest.WebHook
import io.github.broilogabriel.github.model.PullRequestEvent.{Action, Number}

final case class PullRequestEvent(
  action: Action,
  number: Number,
  pullRequest: PullRequest.WebHook,
  repository: Repository,
  sender: User
)

object PullRequestEvent extends ValueClassCodec {

  final case class Action(value: String) extends AnyVal
  final case class Number(value: Long)   extends AnyVal

  val ExternalDecoder: Decoder[PullRequestEvent] = (c: HCursor) =>
    for {
      action      <- c.downField("action").as[Action]
      number      <- c.downField("number").as[Number]
      pullRequest <- c.downField("pull_request").as[PullRequest.WebHook](WebHook.ExternalDecoder)
      repository  <- c.downField("repository").as[Repository]
      sender      <- c.downField("sender").as[User]
    } yield PullRequestEvent(action, number, pullRequest, repository, sender)

  implicit val codec: Codec[PullRequestEvent] = deriveCodec[PullRequestEvent]
}
