package engineering.dd.scalawebapp.utils

import cats.effect.IO
import org.http4s.Request
import org.http4s.headers.Cookie

case class CookieNotFoundError(message: String)
    extends RuntimeException(message)

object CookieUtils {
  def getCookieValue(
      name: String,
      req: Request[IO]
  ): Either[RuntimeException, String] = {
    for {
      header <- req.headers
        .get[Cookie]
        .toRight(new CookieNotFoundError("No cookies found"))
      cookie <- header.values.toList
        .find(_.name == name)
        .toRight(new CookieNotFoundError(s"Could not find cookie $name"))
    } yield cookie.content
  }
}
