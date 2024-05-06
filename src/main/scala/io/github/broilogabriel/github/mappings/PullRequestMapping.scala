package io.github.broilogabriel.github.mappings

import doobie.postgres.circe.jsonb.implicits._
import doobie.postgres.implicits._
import doobie.util.Write

import io.github.broilogabriel.github.model.PullRequest

trait PullRequestMapping {

  implicit val pullRequestWebhookWrite: Write[PullRequest.WebHook] =
    Write[PullRequest.WebHook.Raw].contramap {
      case PullRequest.WebHook(
            id,
            repository,
            user,
            mergeCommitSha,
            state,
            createdAt,
            maybeUpdatedAt,
            maybeClosedAt,
            maybeMergedAt,
            maybeWebHookEventAt
          ) =>
        (
          id,
          repository.id,
          user.id,
          mergeCommitSha,
          state,
          createdAt,
          maybeUpdatedAt,
          maybeClosedAt,
          maybeMergedAt,
          maybeWebHookEventAt
        )
    }
  implicit val pullRequestRefreshWrite: Write[PullRequest.Refresh] =
    Write[PullRequest.Refresh.Raw].contramap {
      case PullRequest.Refresh(
            id,
            repository,
            user,
            maybeMergeCommitSha,
            state,
            createdAt,
            maybeUpdatedAt,
            maybeClosedAt,
            maybeMergedAt,
            maybeSynchronizedAt
          ) =>
        (
          id,
          repository.id,
          user.id,
          maybeMergeCommitSha,
          state,
          createdAt,
          maybeUpdatedAt,
          maybeClosedAt,
          maybeMergedAt,
          maybeSynchronizedAt
        )
    }

}
