package engineering.dd.scalawebapp.auth.refreshtoken

class UtilsSuite extends munit.FunSuite {
  test("create refresh token") {
    val token = Utils.createRefreshToken
    assertEquals(token.length, 32)
  }
}
