package engineering.dd.scalawebapp.auth

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import doobie.util.invariant.UnexpectedEnd
import engineering.dd.scalawebapp.user.User

class RefreshTokenNotFoundError extends Throwable {}

trait RefreshTokenDao {
  def create(
      id: String,
      userId: String,
      tokenHash: String,
      fingerprint: String
  ): IO[RefreshToken]
  def deleteByUserAndFingerprint(
      userId: String,
      fingerprint: String
  ): IO[Unit]
  def getByUserAndRefreshTokenHash(
      userId: String,
      tokenHash: String
  ): IO[Option[RefreshToken]]
}

class RefreshTokenDaoImpl(xa: Transactor[IO]) extends RefreshTokenDao {
  def create(
      id: String,
      userId: String,
      tokenHash: String,
      fingerprint: String
  ): IO[RefreshToken] =
    sql"""
      INSERT INTO refresh_tokens (id, user_id, token_hash, fingerprint)
      VALUES ($id::UUID, $userId::UUID, $tokenHash, $fingerprint)
    """.update
      .withUniqueGeneratedKeys[RefreshToken](
        "id",
        "user_id",
        "token_hash",
        "fingerprint"
      )
      .transact(xa)

  def deleteByUserAndFingerprint(
      userId: String,
      fingerprint: String
  ): IO[Unit] =
    sql"""
      DELETE FROM refresh_tokens
      WHERE user_id = $userId::UUID AND fingerprint = $fingerprint
    """.update.run
      .transact(xa)
      .void
      .handleErrorWith { case UnexpectedEnd =>
        IO.raiseError(new RefreshTokenNotFoundError())
      }

  def getByUserAndRefreshTokenHash(
      userId: String,
      tokenHash: String
  ): IO[Option[RefreshToken]] =
    sql"""
      SELECT id, user_id, token_hash, fingerprint
      FROM refresh_tokens
      WHERE user_id = $userId::UUID AND token_hash = $tokenHash
    """.query[RefreshToken].option.transact(xa)
}
