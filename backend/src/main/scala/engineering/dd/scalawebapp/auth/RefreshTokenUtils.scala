package engineering.dd.scalawebapp.auth

import java.math.BigInteger
import java.security.MessageDigest
import scala.util.Random

object RefreshTokenUtils {
  def createToken = Random.alphanumeric.take(32).mkString
}
