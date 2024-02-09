val scala3Version = "3.3.0"
val http4sVersion = "0.23.24"
val log4catsVersion = "2.6.0"
val doobieVersion = "1.0.0-RC4"
val circeVersion = "0.14.5"
val flywayVersion = "10.1.0"
val testcontainersScalaVersion = "0.41.0"

// Set up Flyway migrations:
enablePlugins(FlywayPlugin)
val dbHost = sys.env.get("POSTGRES_HOST").getOrElse("")
val dbPort = sys.env.get("POSTGRES_PORT").getOrElse("")
val dbName = sys.env.get("POSTGRES_DB").getOrElse("")
val dbUser = sys.env.get("POSTGRES_USER").getOrElse("")
val dbPassword = sys.env.get("POSTGRES_PASSWORD").getOrElse("")
flywayUrl := s"jdbc:postgresql://${dbHost}:${dbPort}/${dbName}"
flywayUser := dbUser
flywayPassword := dbPassword
flywayBaselineOnMigrate := true

lazy val root = project
  .in(file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := "Scala Web App",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    organization := "engineering.dd",
    Compile / run / fork := true,
    // IntegrationTest / fork := true,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % "it,test",
      "org.typelevel" %% "munit-cats-effect" % "2.0.0-M4" % "it,test",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.4",
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-effect" % "3.5.2",
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.10",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion,
      "com.dimafeng" %% "testcontainers-scala-munit" % testcontainersScalaVersion % "it",
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % "it",
      "dev.profunktor" %% "http4s-jwt-auth" % "1.2.1",
      ("com.github.t3hnar" %% "scala-bcrypt" % "4.3.0")
        .cross(CrossVersion.for3Use2_13),
      "com.github.jwt-scala" %% "jwt-circe" % "9.4.5"
    )
  )
