package engineering.dd.scalawebapp.user

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import org.postgresql.util.PSQLException

import java.util.UUID.randomUUID

class UserExistsError extends Throwable {}

case class CreateUserData(email: String, password: String)

trait UserDao {
  def getUserById(id: String): IO[Option[User]]
  def getUserByEmail(email: String): IO[Option[User]]
  def createUser(data: CreateUserData): IO[User]
}

class UserDaoImpl(xa: Transactor[IO]) extends UserDao {
  def getUserById(id: String): IO[Option[User]] = {
    sql"""
      SELECT u.id, u.email, u.password FROM users u
      WHERE u.id = $id::UUID
    """.query[User].option.transact(xa)
  }

  def getUserByEmail(email: String): IO[Option[User]] = {
    sql"""
      SELECT u.id, u.email, u.password FROM users u
      WHERE u.email = $email
    """.query[User].option.transact(xa)
  }

  def createUser(data: CreateUserData): IO[User] = {
    val id = randomUUID().toString()
    sql"""
      INSERT INTO users (id, email, password)
      VALUES (${id}::UUID, ${data.email}, ${data.password})
    """.update
      .withUniqueGeneratedKeys[User]("id", "email", "password")
      .transact(xa)
      .exceptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION =>
        IO.raiseError(new UserExistsError())
      }
  }
}
