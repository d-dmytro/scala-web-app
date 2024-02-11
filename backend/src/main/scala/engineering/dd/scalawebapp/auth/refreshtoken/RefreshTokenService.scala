package engineering.dd.scalawebapp.auth.refreshtoken

import cats.effect.IO
import engineering.dd.scalawebapp.AuthConfig
import engineering.dd.scalawebapp.utils.HashingUtils
import io.circe.generic.auto.*

import java.util.UUID.randomUUID

trait RefreshTokenService {
  def createRefreshToken(
      userId: String,
      fingerprint: String
  ): IO[String]
  def refreshToken(
      userId: String,
      oldRefreshToken: String,
      fingerprint: String
  ): IO[String]
  def deleteUserRefreshTokens(userId: String, fingerprint: String): IO[Unit]
}

class RefreshTokenServiceImpl(
    config: AuthConfig,
    refreshTokenDao: RefreshTokenDao
) extends RefreshTokenService {
  def createRefreshToken(
      userId: String,
      fingerprint: String
  ): IO[String] = {
    val refreshTokenStr = Utils.createRefreshToken
    refreshTokenDao
      .create(
        id = randomUUID.toString,
        userId,
        HashingUtils.hashString(refreshTokenStr),
        HashingUtils.hashString(fingerprint)
      )
      .as(refreshTokenStr)
  }

  def refreshToken(
      userId: String,
      oldRefreshToken: String,
      fingerprint: String
  ): IO[String] = {
    refreshTokenDao
      .getByUserAndRefreshTokenHash(
        userId,
        HashingUtils.hashString(oldRefreshToken)
      )
      .flatMap {
        case Some(_) =>
          for {
            _ <- refreshTokenDao.deleteByUserAndFingerprint(
              userId,
              HashingUtils.hashString(fingerprint)
            )
            refreshTokenStr <- createRefreshToken(userId, fingerprint)
          } yield refreshTokenStr
        case None =>
          refreshTokenDao
            .deleteByUserAndFingerprint(
              userId,
              HashingUtils.hashString(fingerprint)
            )
            >> IO.raiseError(new RefreshTokenNotFoundError())
      }
  }

  def deleteUserRefreshTokens(userId: String, fingerprint: String): IO[Unit] =
    refreshTokenDao.deleteByUserAndFingerprint(
      userId,
      HashingUtils.hashString(fingerprint)
    )
}
