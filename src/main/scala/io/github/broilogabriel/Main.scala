package io.github.broilogabriel

import cats.effect._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp.Simple {
  val run: IO[Nothing] = {
    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
    Server.run[IO]
  }
}
