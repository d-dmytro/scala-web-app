package engineering.dd.scalawebapp

import cats.effect.IO
import pureconfig.*
import pureconfig.generic.derivation.default.*

case class DbConfig(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String
) derives ConfigReader

case class ServerConfig(host: String, port: Int, devCorsSetup: Int)
    derives ConfigReader

case class AuthConfig(jwtSecret: String) derives ConfigReader

case class Config(server: ServerConfig, db: DbConfig, auth: AuthConfig)
    derives ConfigReader

object Config {
  def load = ConfigSource.default.load[Config] match {
    case Right(config) => IO(config)
    case Left(errors)  => IO.raiseError(new RuntimeException(errors.toString))
  }
}
