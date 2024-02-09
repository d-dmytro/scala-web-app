package engineering.dd.scalawebapp.user

case class User(id: String, email: String, password: String)

case class UserDto(id: String, email: String)

def userToDto(user: User): UserDto = UserDto(user.id, user.email)
