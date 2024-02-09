package engineering.dd.scalawebapp.utils

import java.math.BigInteger
import java.security.MessageDigest

object HashingUtils {
  def hashString(original: String): String =
    String.format(
      "%064x",
      new BigInteger(
        1,
        MessageDigest.getInstance("SHA-256").digest(original.getBytes)
      )
    )
}
