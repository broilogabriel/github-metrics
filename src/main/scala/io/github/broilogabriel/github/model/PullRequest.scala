package io.github.broilogabriel.github.model

import java.time.Instant

import io.circe.{Codec, Decoder, HCursor}
import io.circe.generic.semiauto

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.PullRequest.{
  ClosedAt,
  CreatedAt,
  Id,
  MergeCommitSha,
  MergedAt,
  State,
  UpdatedAt
}
sealed trait PullRequest {
  val id: Id
  val repository: Repository
  val user: User
  val mergeCommitSha: Option[MergeCommitSha]
  val state: State
  val createdAt: CreatedAt
  val updatedAt: Option[UpdatedAt]
  val closedAt: Option[ClosedAt]
  val mergedAt: Option[MergedAt]
}
object PullRequest extends ValueClassCodec {

  final case class Id(value: Long)                extends AnyVal
  final case class MergeCommitSha(value: String)  extends AnyVal
  final case class State(value: String)           extends AnyVal
  final case class CreatedAt(value: Instant)      extends AnyVal
  final case class UpdatedAt(value: Instant)      extends AnyVal
  final case class ClosedAt(value: Instant)       extends AnyVal
  final case class MergedAt(value: Instant)       extends AnyVal
  final case class SynchronizedAt(value: Instant) extends AnyVal
  object SynchronizedAt {
    def now: SynchronizedAt = SynchronizedAt(Instant.now())
  }
  final case class WebHookEventAt(value: Instant) extends AnyVal
  object WebHookEventAt {
    def now: WebHookEventAt = WebHookEventAt(Instant.now())
  }

  final case class WebHook(
    id: Id,
    repository: Repository,
    user: User,
    mergeCommitSha: Option[MergeCommitSha],
    state: State,
    createdAt: CreatedAt,
    updatedAt: Option[UpdatedAt],
    closedAt: Option[ClosedAt],
    mergedAt: Option[MergedAt],
    webHookEventAt: Option[WebHookEventAt]
  ) extends PullRequest

  object WebHook {
    type Raw = (
      PullRequest.Id,
      Repository.Id,
      User.Id,
      Option[MergeCommitSha],
      State,
      CreatedAt,
      Option[UpdatedAt],
      Option[ClosedAt],
      Option[MergedAt],
      Option[WebHookEventAt]
    )
    implicit val codec: Codec[WebHook] = semiauto.deriveCodec[WebHook]
    val ExternalDecoder: Decoder[WebHook] = (c: HCursor) =>
      for {
        id             <- c.downField("id").as[Id]
        repository     <- c.downField("base").downField("repo").as[Repository]
        user           <- c.downField("user").as[User]
        mergeCommitSha <- c.downField("mergeCommitSha").as[Option[MergeCommitSha]]
        state          <- c.downField("state").as[State]
        createdAt      <- c.downField("created_at").as[CreatedAt]
        updatedAt      <- c.downField("updated_at").as[Option[UpdatedAt]]
        closedAt       <- c.downField("closed_at").as[Option[ClosedAt]]
        mergedAt       <- c.downField("merged_at").as[Option[MergedAt]]
      } yield new WebHook(
        id,
        repository,
        user,
        mergeCommitSha,
        state,
        createdAt,
        updatedAt,
        closedAt,
        mergedAt,
        Option(WebHookEventAt.now)
      )

    def apply(pullRequest: github4s.domain.PullRequest, webHookEventAt: WebHookEventAt): PullRequest.WebHook =
      new WebHook(
        Id(pullRequest.id),
        pullRequest.base.flatMap(_.repo.map(Repository(_, Option.empty))).get,
        pullRequest.user.map(User.apply).get,
        pullRequest.merge_commit_sha.map(MergeCommitSha.apply),
        State(pullRequest.state),
        CreatedAt(Instant.parse(pullRequest.created_at)),
        pullRequest.updated_at.map(u => UpdatedAt(Instant.parse(u))),
        pullRequest.closed_at.map(u => ClosedAt(Instant.parse(u))),
        pullRequest.merged_at.map(u => MergedAt(Instant.parse(u))),
        Option(webHookEventAt)
      )
  }

  final case class Refresh(
    id: Id,
    repository: Repository,
    user: User,
    mergeCommitSha: Option[MergeCommitSha],
    state: State,
    createdAt: CreatedAt,
    updatedAt: Option[UpdatedAt],
    closedAt: Option[ClosedAt],
    mergedAt: Option[MergedAt],
    synchronizedAt: Option[SynchronizedAt]
  ) extends PullRequest

  object Refresh {
    type Raw = (
      PullRequest.Id,
      Repository.Id,
      User.Id,
      Option[MergeCommitSha],
      State,
      CreatedAt,
      Option[UpdatedAt],
      Option[ClosedAt],
      Option[MergedAt],
      Option[SynchronizedAt]
    )
    def apply(pullRequest: github4s.domain.PullRequest, synchronizedAt: SynchronizedAt): PullRequest.Refresh =
      new Refresh(
        Id(pullRequest.id),
        pullRequest.base.flatMap(_.repo.map(Repository(_, Option.empty))).get,
        pullRequest.user.map(User.apply).get,
        pullRequest.merge_commit_sha.map(MergeCommitSha.apply),
        State(pullRequest.state),
        CreatedAt(Instant.parse(pullRequest.created_at)),
        pullRequest.updated_at.map(u => UpdatedAt(Instant.parse(u))),
        pullRequest.closed_at.map(u => ClosedAt(Instant.parse(u))),
        pullRequest.merged_at.map(u => MergedAt(Instant.parse(u))),
        Option(synchronizedAt)
      )
  }

}
