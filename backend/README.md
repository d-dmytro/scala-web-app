## sbt project compiled with Scala 3

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).

### Database migrations

There are two ways to create migrations:

1. SQL files in `src/main/resources/migration`
2. Scala files in `src/main/scala/db/migration`

To run migrations locally:

```
eval $(cat .env) sbt flywayMigrate
```

### Tests

To run integration tests:

```
sbt "IntegrationTest/test"
```

To run unit tests:

```
sbt test
```

### Running the app

Copy `.env.example` to `.env` and fill it in with correct credentials.

Start dependencies using Docker Compose:

```
docker compose up -d
```

This will start Postgres and Adminer.

Start the app:

```
sbt run
```
