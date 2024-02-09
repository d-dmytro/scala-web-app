package engineering.dd.scalawebapp

import cats.effect.IO
import org.flywaydb.core.Flyway

case class MigrationConfig(
    url: String,
    user: String,
    password: String
)

object Migration {
  def migrate(config: MigrationConfig): Unit = {
    val flyway = Flyway
      .configure()
      .dataSource(
        config.url,
        config.user,
        config.password
      )
      .load()
    flyway.migrate()
  }
}
