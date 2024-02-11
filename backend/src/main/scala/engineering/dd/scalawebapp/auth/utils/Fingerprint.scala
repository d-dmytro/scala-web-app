package engineering.dd.scalawebapp.auth.utils

import scala.util.Random

object Fingerprint {
  def create = Random.alphanumeric.take(32).mkString
}
