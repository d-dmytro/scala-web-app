package engineering.dd.scalawebapp.auth

import cats.effect.IO
import engineering.dd.scalawebapp.user.UserExistsError
import engineering.dd.scalawebapp.user.userToDto
import engineering.dd.scalawebapp.utils.CookieNotFoundError
import engineering.dd.scalawebapp.utils.CookieUtils
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.EntityDecoder
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.ResponseCookie
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.typelevel.log4cats.LoggerFactory

case class SignInBody(email: String, password: String)
case class SignUpBody(email: String, password: String)
case class RefreshBody(userId: String)
case class ErrorBody(messages: List[String])

implicit val signInBodyDecoder: EntityDecoder[IO, SignInBody] =
  jsonOf[IO, SignInBody]

implicit val signUpBodyDecoder: EntityDecoder[IO, SignUpBody] =
  jsonOf[IO, SignUpBody]

implicit val refreshBodyDecoder: EntityDecoder[IO, RefreshBody] =
  jsonOf[IO, RefreshBody]

object AuthRoutes {
  def make(authService: AuthService)(implicit
      loggerFactory: LoggerFactory[IO]
  ): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "signin" =>
        for {
          body <- req.as[SignInBody]
          resultOption <- authService.signIn(body.email, body.password)
          res <- resultOption match {
            case Some(result) =>
              Ok(userToDto(result.user).asJson)
                .map(
                  addAuthCookies(
                    _,
                    result.accessToken,
                    result.refreshToken,
                    result.fingerprint
                  )
                )
            case None => Forbidden()
          }
        } yield res

      case req @ POST -> Root / "signup" =>
        for {
          body <- req.as[SignUpBody]
          res <- authService
            .signUp(SignUpInput(body.email, body.password))
            .flatMap { result =>
              Ok(userToDto(result.user).asJson)
                .map(
                  addAuthCookies(
                    _,
                    result.accessToken,
                    result.refreshToken,
                    result.fingerprint
                  )
                )
            }
            .handleErrorWith { case _: UserExistsError =>
              Conflict(
                ErrorBody(List(s"User ${body.email} exists")).asJson
              )
            }
        } yield res

      case req @ POST -> Root / "refresh" =>
        (for {
          body <- req.as[RefreshBody]
          refreshToken <- IO.fromEither(
            CookieUtils.getCookieValue("refresh-token", req)
          )
          fingerprint <- IO.fromEither(
            CookieUtils.getCookieValue("fingerprint", req)
          )
          res <- authService
            .refreshToken(body.userId, refreshToken, fingerprint)
            .flatMap { result =>
              Ok().map(
                addAuthCookies(
                  _,
                  result.accessToken,
                  result.refreshToken,
                  fingerprint
                )
              )
            }
        } yield res)
          .handleErrorWith {
            case _: RefreshTokenNotFoundError =>
              Conflict(
                ErrorBody(List("Could not refresh your token")).asJson
              )
            case _: CookieNotFoundError =>
              BadRequest(ErrorBody(List("Auth cookie not found")).asJson)
          }

      case req @ POST -> Root / "signout" =>
        (for {
          accessToken <- IO.fromEither(CookieUtils.getCookieValue("jwt", req))
          fingerprint <- IO.fromEither(
            CookieUtils.getCookieValue("fingerprint", req)
          )
          _ <- authService.signOut(accessToken, fingerprint)
          res <- Ok()
            .map(_.removeCookie("jwt"))
            .map(_.removeCookie("refresh-token"))
            .map(_.removeCookie("fingerprint"))
        } yield res).handleErrorWith { case _: CookieNotFoundError =>
          BadRequest(ErrorBody(List("Could not find auth cookie")).asJson)
        }
    }

  private def addAuthCookies(
      res: Response[IO],
      accessToken: String,
      refreshToken: String,
      fingerprint: String
  ): Response[IO] = {
    res
      .addCookie(makeAccessTokenCookie(accessToken))
      .addCookie(makeRefreshTokenCookie(refreshToken))
      .addCookie(makeFingerprintCookie(fingerprint))
  }

  private def makeAccessTokenCookie(jwt: String): ResponseCookie =
    ResponseCookie(
      "jwt",
      jwt,
      httpOnly = true
    )

  private def makeRefreshTokenCookie(jwt: String): ResponseCookie =
    ResponseCookie(
      "refresh-token",
      jwt,
      httpOnly = true,
      path = Some("/refresh")
    )

  private def makeFingerprintCookie(fingerprint: String): ResponseCookie =
    ResponseCookie(
      "fingerprint",
      fingerprint,
      httpOnly = true
    )
}
