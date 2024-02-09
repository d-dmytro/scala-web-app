package engineering.dd.scalawebapp.auth

import scala.util.Random

object FingerprintUtils {
  def createFingerprint = Random.alphanumeric.take(32).mkString
}
