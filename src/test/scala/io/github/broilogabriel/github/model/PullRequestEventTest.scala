package io.github.broilogabriel.github.model

import scala.io.Source

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import io.circe._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import io.github.broilogabriel.github.model.PullRequestEvent.{Action, ExternalDecoder, Number}

class PullRequestEventTest extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "pull request event decoding" when {
    implicit val decoder: Decoder[PullRequestEvent] = ExternalDecoder
    "event action is opened" should {
      val resourcePath = "webhook/pull_request_opened.json"
      "return the correct result" in {
        resourceHandler(resourcePath)
          .map(parser.decode[PullRequestEvent])
          .allocated
          .asserting {
            case (Right(PullRequestEvent(action, number, pullRequest, repository, sender)), _) =>
              number shouldBe Number(1)
              action shouldBe Action("opened")
              pullRequest.id shouldBe PullRequest.Id(1832873486)
              repository.id shouldBe Repository.Id(785430811)
              sender.id shouldBe User.Id(5182223)
            case (Left(err), _) => fail(s"Invalid branch with $err")
          }
      }
    }
  }
  "other event decoding" should {
    implicit val decoder: Decoder[PullRequestEvent] = ExternalDecoder
    val resourcePath                                = "webhook/workflow_run_requested.json"
    "return a failure to decode as pull request" in {
      resourceHandler(resourcePath)
        .map(parser.decode[PullRequestEvent])
        .allocated
        .asserting {
          case (Right(_), _) => fail(s"Invalid branch, it should not decode")
          case (Left(_), _)  => succeed
        }
    }
  }

  def resourceHandler(filename: String): Resource[IO, String] =
    Resource
      .make { IO(Source.fromResource(filename)) } { source => IO(source.close()).handleErrorWith(_ => IO.unit) }
      .map(source => source.getLines().reduce(_ + _))

}
