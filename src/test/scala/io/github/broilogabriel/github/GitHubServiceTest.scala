package io.github.broilogabriel.github

import java.time.Instant
import java.time.temporal.ChronoUnit

import cats.effect.testing.scalatest.AsyncIOSpec
import github4s.domain.Pagination
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import io.github.broilogabriel.github.model.{PullRequest, Repository, User}
import io.github.broilogabriel.github.model.PullRequest.{CreatedAt, State}
import io.github.broilogabriel.github.model.Repository.SynchronizedAt

class GitHubServiceTest extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "getNextPage" when {
    val user = User(User.Id(1), User.Login("someuser"), User.Type("user"))
    val repo = Repository(Repository.Id(1), user, Repository.Name("somerepo"), Option(Repository.SynchronizedAt.now))
    val prUpdatedNow = Option(PullRequest.UpdatedAt(Instant.now()))
    val pullRequests: Option[PullRequest.UpdatedAt] => Seq[PullRequest.Refresh] = updatedAt =>
      Seq(
        PullRequest.Refresh(
          PullRequest.Id(1),
          repo,
          user,
          Option.empty[PullRequest.MergeCommitSha],
          State("open"),
          CreatedAt(Instant.now()),
          updatedAt,
          Option.empty[PullRequest.ClosedAt],
          Option.empty[PullRequest.MergedAt],
          Option.empty[PullRequest.SynchronizedAt]
        )
      )
    val lastSync = Option(SynchronizedAt(Instant.now()))
    "pull requests" when {
      "is empty" should {
        "return an empty next page" in {
          GitHubService.getNextPage(GitHubService.FIRST_PAGE, lastSync, Seq.empty[PullRequest.Refresh]) should be(
            Option.empty[Pagination]
          )
        }
      }
      "contains empty update at" should {
        "return an empty next page" in {
          GitHubService.getNextPage(GitHubService.FIRST_PAGE, lastSync, pullRequests(Option.empty)) should be(
            Option.empty[Pagination]
          )
        }
      }
    }
    "lastSync" when {
      "is empty" should {
        "return the next page" in {
          GitHubService.getNextPage(GitHubService.FIRST_PAGE, Option.empty, pullRequests(prUpdatedNow)) should be(
            Option(GitHubService.FIRST_PAGE.copy(page = 2))
          )
        }
      }
      "is before the oldest pr update" should {
        "return the next page" in {
          GitHubService.getNextPage(
            GitHubService.FIRST_PAGE,
            Option(SynchronizedAt(Instant.now().minus(2, ChronoUnit.HOURS))),
            pullRequests(prUpdatedNow)
          ) should be(
            Option(GitHubService.FIRST_PAGE.copy(page = 2))
          )
        }
      }
      "is after the oldest pr update" should {
        "return the next page" in {
          GitHubService.getNextPage(
            GitHubService.FIRST_PAGE,
            Option(SynchronizedAt.now),
            pullRequests(Option(PullRequest.UpdatedAt(Instant.now().minus(2, ChronoUnit.HOURS))))
          ) should be(
            Option.empty[Pagination]
          )
        }
      }
    }
  }

}
