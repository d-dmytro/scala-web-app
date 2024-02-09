package engineering.dd.scalawebapp

import cats.effect.IO
import com.comcast.ip4s.Ipv4Address
import com.comcast.ip4s.Port
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.ErrorAction
import org.http4s.server.middleware.ErrorHandling
import org.typelevel.log4cats.LoggerFactory

trait Server {
  def start: IO[Nothing]
}

class ServerImpl(
    config: ServerConfig,
    httpApp: HttpApp[IO]
)(implicit
    loggerFactory: LoggerFactory[IO]
) extends Server {
  val httpAppWitErrorLogging = ErrorHandling.Recover.total(
    ErrorAction.log(
      httpApp,
      messageFailureLogAction =
        (error, message) => loggerFactory.getLogger.error(error)(message),
      serviceErrorLogAction =
        (error, message) => loggerFactory.getLogger.error(error)(message)
    )
  )

  val emberServer = EmberServerBuilder
    .default[IO]
    .withHost(Ipv4Address.fromString(config.host).get)
    .withPort(Port.fromInt(config.port).get)
    .withHttpApp(httpAppWitErrorLogging)
    .build

  def start: IO[Nothing] = emberServer.use(_ => IO.never)
}
