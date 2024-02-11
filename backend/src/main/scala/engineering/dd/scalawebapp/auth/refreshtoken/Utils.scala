package engineering.dd.scalawebapp.auth.refreshtoken

import java.math.BigInteger
import java.security.MessageDigest
import scala.util.Random

object Utils {
  def createRefreshToken = Random.alphanumeric.take(32).mkString
}
