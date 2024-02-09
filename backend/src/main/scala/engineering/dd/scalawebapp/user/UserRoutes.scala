package engineering.dd.scalawebapp.user

import cats.effect.IO
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.AuthedRoutes
import org.http4s.circe.*
import org.http4s.dsl.io.*

object UserRoutes {
  def make(userDao: UserDao): AuthedRoutes[User, IO] = AuthedRoutes.of {
    case GET -> Root / "me" as user =>
      Ok(userToDto(user).asJson)
  }
}
