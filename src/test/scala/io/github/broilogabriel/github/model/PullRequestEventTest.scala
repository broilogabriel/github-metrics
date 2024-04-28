package io.github.broilogabriel.github.model

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

import cats.effect._
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResource}
import io.circe._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}

import io.github.broilogabriel.github.model.PullRequestEvent.{Action, Number}

class PullRequestEventTest extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "pull request event decoding" when {
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

  def resourceHandler(filename: String): Resource[IO, String] =
    Resource
      .make { IO(Source.fromResource(filename)) } { source => IO(source.close()).handleErrorWith(_ => IO.unit) }
      .map(source => source.getLines().reduce(_ + _))

}
