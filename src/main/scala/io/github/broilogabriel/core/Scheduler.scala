package io.github.broilogabriel.core

import cats.effect.Temporal
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import org.typelevel.log4cats.{LoggerFactory, SelfAwareStructuredLogger}

private[core] class Scheduler[F[_]: LoggerFactory: Temporal](config: Config, service: Service[F]) {
  private val logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
  def start: F[Unit] = (for {
    _ <- Cron4sScheduler.utc.awakeEvery(Cron.unsafeParse(config.github.cron))
    _ <- fs2.Stream.eval(logger.debug(s"Scheduled job: ${config.github.cron}"))
    _ <- fs2.Stream.eval(service.gitHubService.synchronizePullRequests)
  } yield ()).compile.drain

}

object Scheduler {
  def apply[F[_]: LoggerFactory: Temporal](config: Config, service: Service[F]): Scheduler[F] =
    new Scheduler(config, service)
}
