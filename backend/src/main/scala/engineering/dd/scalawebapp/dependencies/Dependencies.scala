package engineering.dd.scalawebapp.dependencies

import cats.effect.IO
import cats.effect.Resource
import cats.effect.unsafe.IORuntime
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import engineering.dd.scalawebapp.Config
import engineering.dd.scalawebapp.Server
import engineering.dd.scalawebapp.ServerImpl
import engineering.dd.scalawebapp.auth.AuthMiddleware
import engineering.dd.scalawebapp.auth.AuthRoutes
import engineering.dd.scalawebapp.auth.AuthService
import engineering.dd.scalawebapp.auth.AuthServiceImpl
import engineering.dd.scalawebapp.auth.RefreshTokenDao
import engineering.dd.scalawebapp.auth.RefreshTokenDaoImpl
import engineering.dd.scalawebapp.auth.RefreshTokenService
import engineering.dd.scalawebapp.auth.RefreshTokenServiceImpl
import engineering.dd.scalawebapp.invoice.InvoiceRoutes
import engineering.dd.scalawebapp.invoice.InvoiceService
import engineering.dd.scalawebapp.invoice.InvoiceServiceImpl
import engineering.dd.scalawebapp.user.UserDao
import engineering.dd.scalawebapp.user.UserDaoImpl
import engineering.dd.scalawebapp.user.UserRoutes
import org.http4s.HttpApp
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class Dependencies(
    config: Config,
    val allocator: Allocator
) {
  implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  lazy val xa: Transactor[IO] = allocator.allocate {
    for {
      threadPool <- ExecutionContexts.fixedThreadPool[IO](16)
      transactor <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        s"jdbc:postgresql://${config.db.host}:${config.db.port}/${config.db.database}",
        config.db.user,
        config.db.password,
        threadPool
      )
    } yield transactor
  }

  lazy val userDao: UserDao = new UserDaoImpl(xa)

  lazy val refreshTokenDao: RefreshTokenDao = new RefreshTokenDaoImpl(xa)

  lazy val refreshTokenService: RefreshTokenService =
    new RefreshTokenServiceImpl(config.auth, refreshTokenDao)

  lazy val authService: AuthService =
    new AuthServiceImpl(config.auth, userDao, refreshTokenService)

  lazy val invoiceService: InvoiceService = new InvoiceServiceImpl(xa)

  lazy val authMiddleware = AuthMiddleware.make(authService, userDao)

  lazy val cors =
    if (config.server.devCorsSetup == 1)
      CORS.policy
        .withAllowOriginHost(_ => true)
        .withAllowCredentials(true)
    else CORS.policy

  lazy val httpApp: HttpApp[IO] = cors.apply(
    AuthRoutes
      .make(authService)
      .combineK(authMiddleware(UserRoutes.make(userDao)))
      .combineK(InvoiceRoutes.make(invoiceService))
      .orNotFound
  )

  lazy val server: Server = new ServerImpl(config.server, httpApp)
}

object Dependencies {
  def apply(
      config: Config,
      runtime: IORuntime
  ): Resource[IO, Dependencies] =
    Resource.make(IO(unsafeCreate(config, runtime)))(_.allocator.shutdownAll)

  private def unsafeCreate(
      config: Config,
      runtime: IORuntime
  ): Dependencies =
    new Dependencies(config, new Allocator()(runtime))
}
