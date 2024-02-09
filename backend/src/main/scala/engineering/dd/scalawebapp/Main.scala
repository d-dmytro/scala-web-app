package engineering.dd.scalawebapp

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.unsafe.IORuntime
import engineering.dd.scalawebapp.dependencies.Dependencies

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- Config.load
      exitCode <- Dependencies(config, IORuntime.global).use(
        _.server.start.as(ExitCode.Success)
      )
    } yield exitCode
}
