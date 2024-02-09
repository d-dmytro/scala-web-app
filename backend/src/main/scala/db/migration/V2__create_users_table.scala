package db.migration

import org.flywaydb.core.api.migration.{BaseJavaMigration, Context}

class V2__create_users_table extends BaseJavaMigration {
  override def migrate(context: Context): Unit = {
    val conn = context.getConnection

    conn
      .createStatement()
      .executeUpdate(
        """
        |CREATE TABLE users (
        |  id UUID NOT NULL,
        |  email VARCHAR NOT NULL,
        |  password VARCHAR NOT NULL,
        |  PRIMARY KEY (id),
        |  UNIQUE(email)
        |);
      """.stripMargin
      )
  }
}
