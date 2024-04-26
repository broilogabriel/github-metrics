package io.github.broilogabriel.github.model

import java.time.Instant

import io.circe.{Decoder, HCursor}

import io.github.broilogabriel.core.ValueClassCodec
import io.github.broilogabriel.github.model.PullRequest.{
  ClosedAt,
  CreatedAt,
  Id,
  MergeCommitSha,
  MergedAt,
  State,
  SynchronizedAt,
  UpdatedAt
}

final case class PullRequest(
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
)

object PullRequest extends ValueClassCodec {
  final case class Id(value: Long)                extends AnyVal
  final case class MergeCommitSha(value: String)  extends AnyVal
  final case class State(value: String)           extends AnyVal
  final case class CreatedAt(value: Instant)      extends AnyVal
  final case class UpdatedAt(value: Instant)      extends AnyVal
  final case class ClosedAt(value: Instant)       extends AnyVal
  final case class MergedAt(value: Instant)       extends AnyVal
  final case class SynchronizedAt(value: Instant) extends AnyVal

  def apply(pullRequest: github4s.domain.PullRequest, synchronizedAt: SynchronizedAt): PullRequest = new PullRequest(
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
