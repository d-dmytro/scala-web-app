package engineering.dd.scalawebapp.auth

import cats.effect.IO
import com.github.t3hnar.bcrypt.*
import engineering.dd.scalawebapp.AuthConfig
import engineering.dd.scalawebapp.user.CreateUserData
import engineering.dd.scalawebapp.user.User
import engineering.dd.scalawebapp.user.UserDao
import io.circe.generic.auto.*

case class SignInResult(
    user: User,
    accessToken: String,
    refreshToken: String,
    fingerprint: String
)

case class SignUpInput(email: String, password: String)

case class SignUpResult(
    user: User,
    accessToken: String,
    refreshToken: String,
    fingerprint: String
)

case class RefreshTokenResult(
    accessToken: String,
    refreshToken: String
)

case class AccessTokenPayload(userId: String) extends TokenPayload

trait AuthService {
  def signIn(email: String, password: String): IO[Option[SignInResult]]
  def signUp(input: SignUpInput): IO[SignUpResult]
  def signOut(accessToken: String, fingerprint: String): IO[Unit]
  def verifyAccessToken(
      accessToken: String,
      checkExpiration: Boolean = true
  ): IO[AccessTokenPayload]
  def refreshToken(
      userId: String,
      oldRefreshToken: String,
      fingerprint: String
  ): IO[RefreshTokenResult]
}

class AuthServiceImpl(
    config: AuthConfig,
    userDao: UserDao,
    refreshTokenService: RefreshTokenService
) extends AuthService {
  def signIn(email: String, password: String): IO[Option[SignInResult]] = for {
    user <- getUserAndValidatePassword(email, password)
    signInData <- user match {
      case Some(user) => {
        val fingerprint = FingerprintUtils.createFingerprint
        val accessToken = createAccessToken(user.id)
        refreshTokenService
          .createRefreshToken(user.id, fingerprint)
          .map { refreshToken =>
            Some(
              SignInResult(
                user,
                accessToken,
                refreshToken,
                fingerprint
              )
            )
          }
      }
      case None => IO.none[SignInResult]
    }
  } yield signInData

  def signUp(input: SignUpInput): IO[SignUpResult] = {
    val fingerprint = FingerprintUtils.createFingerprint
    for {
      hashedPassword <- IO.fromTry(input.password.bcryptSafeBounded)
      user <- userDao.createUser(CreateUserData(input.email, hashedPassword))
      accessToken = createAccessToken(user.id)
      refreshToken <- refreshTokenService.createRefreshToken(
        user.id,
        fingerprint
      )
    } yield SignUpResult(
      user,
      accessToken,
      refreshToken,
      fingerprint
    )
  }

  def signOut(accessToken: String, fingerprint: String): IO[Unit] = for {
    payload <- verifyAccessToken(accessToken, checkExpiration = false)
    _ <- refreshTokenService.deleteUserRefreshTokens(
      payload.userId,
      fingerprint
    )
  } yield {}

  def verifyAccessToken(
      accessToken: String,
      checkExpiration: Boolean = true
  ): IO[AccessTokenPayload] =
    JwtUtils.decodeJwt[AccessTokenPayload](
      accessToken,
      config.jwtSecret,
      checkExpiration
    )

  def refreshToken(
      userId: String,
      oldRefreshToken: String,
      fingerprint: String
  ): IO[RefreshTokenResult] = {
    refreshTokenService
      .refreshToken(userId, oldRefreshToken, fingerprint)
      .map { refreshToken =>
        RefreshTokenResult(
          createAccessToken(userId),
          refreshToken
        )
      }
  }

  private def createAccessToken(userId: String) =
    JwtUtils.createJwt(
      AccessTokenPayload(userId),
      AccessTokenAge,
      config.jwtSecret
    )

  private def getUserAndValidatePassword(
      email: String,
      password: String
  ): IO[Option[User]] = for {
    userOption <- userDao.getUserByEmail(email)
    isPasswordValid <- userOption match {
      case Some(user) =>
        IO.fromTry(password.isBcryptedSafeBounded(user.password))
      case None => IO(false)
    }
  } yield if (isPasswordValid) userOption else None
}
