package engineering.dd.scalawebapp.auth

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.IO
import engineering.dd.scalawebapp.user.User
import engineering.dd.scalawebapp.user.UserDao
import engineering.dd.scalawebapp.utils.CookieNotFoundError
import engineering.dd.scalawebapp.utils.CookieUtils
import engineering.dd.scalawebapp.utils.ErrorResponse
import io.circe.generic.auto.*
import org.http4s.AuthedRoutes
import org.http4s.Request
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.server.AuthMiddleware as Http4sAuthMiddleware

case class UserNotFoundError(message: String) extends Exception(message)

object AuthMiddleware {
  def make(authService: AuthService, userDao: UserDao) = {
    val authUserCookie: Kleisli[IO, Request[IO], Either[Throwable, User]] =
      Kleisli({ req =>
        CookieUtils.getCookieValue("jwt", req) match {
          case Right(accessToken) =>
            authService
              .verifyAccessToken(accessToken)
              .flatMap(payload => userDao.getUserById(payload.userId))
              .map(_.toRight(new UserNotFoundError("Could not find user")))
              .handleError(error => Left(error))
          case Left(error) => IO(Left(error))
        }
      })

    val onFailure: AuthedRoutes[Throwable, IO] = {
      def getForbiddenResponse(m: String) = Forbidden(
        ErrorResponse.getErrorResponse(m)
      )
      Kleisli(req => {
        req.context match {
          case CookieNotFoundError(m) => OptionT.liftF(getForbiddenResponse(m))
          case UserNotFoundError(m)   => OptionT.liftF(getForbiddenResponse(m))
          case JwtExpiredError(m)     => OptionT.liftF(getForbiddenResponse(m))
        }
      })
    }

    Http4sAuthMiddleware(authUserCookie, onFailure)
  }
}
