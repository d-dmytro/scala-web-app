package engineering.dd.scalawebapp.auth

import cats.effect.IO
import io.circe.Decoder
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax.*
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtCirce
import pdi.jwt.JwtClaim

import java.time.Instant
import pdi.jwt.JwtOptions
import pdi.jwt.exceptions.JwtExpirationException

trait TokenPayload

case class JwtExpiredError(message: String) extends Exception(message)

object JwtUtils {
  def decodeJwt[P <: TokenPayload: Decoder](
      jwt: String,
      secretKey: String,
      checkExpiration: Boolean = true
  ): IO[P] =
    val jwtOptions = JwtOptions(
      expiration = checkExpiration
    )
    (for {
      claim <- IO
        .fromTry(
          JwtCirce.decode(jwt, secretKey, Seq(JwtAlgorithm.HS256), jwtOptions)
        )
      payload <- IO.fromEither(decode[P](claim.content))
    } yield payload)
      .handleErrorWith {
        case error: JwtExpirationException =>
          IO.raiseError(new JwtExpiredError("JWT expired"))
        case other => IO.raiseError(other)
      }

  def createJwt[P <: TokenPayload: Encoder](
      payload: P,
      duration: Long,
      secretKey: String
  ): String =
    val now = Instant.now
    val claim = JwtClaim(
      content = payload.asJson.noSpaces,
      expiration = Some(now.plusSeconds(duration).getEpochSecond),
      issuedAt = Some(now.getEpochSecond)
    )
    JwtCirce.encode(claim, secretKey, JwtAlgorithm.HS256)
}
