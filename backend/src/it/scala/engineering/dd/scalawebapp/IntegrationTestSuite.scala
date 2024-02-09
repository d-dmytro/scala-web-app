package engineering.dd.scalawebapp

import cats.effect.IO
import cats.effect.Resource
import cats.effect.unsafe.IORuntime
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainersForAll
import engineering.dd.scalawebapp.dependencies.Dependencies
import munit.AnyFixture
import munit.CatsEffectSuite
import munit.Suite
import munit.catseffect.ResourceFixture

trait IntegrationTestSuite extends CatsEffectSuite with TestContainersForAll {
  self: Suite =>
  override type Containers = PostgreSQLContainer

  override def startContainers(): Containers = {
    PostgreSQLContainer.Def().start()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    withContainers { case postgresContainer =>
      Migration
        .migrate(
          MigrationConfig(
            postgresContainer.jdbcUrl,
            postgresContainer.username,
            postgresContainer.password
          )
        )
    }
  }

  val configResource = Resource
    .make(IO(withContainers { case postgresContainer =>
      Config(
        ServerConfig(
          host = "localhost",
          port = 8000,
          devCorsSetup = 0
        ),
        DbConfig(
          postgresContainer.host,
          postgresContainer.mappedPort(5432),
          postgresContainer.username,
          postgresContainer.password,
          postgresContainer.databaseName
        ),
        AuthConfig("somejwtsecret")
      )
    }))(_ => IO.unit)

  val dependenciesFixture = ResourceSuiteLocalFixture(
    "dependencies",
    configResource.flatMap(config => Dependencies(config, IORuntime.global))
  )

  override def munitFixtures = List(dependenciesFixture)
}
