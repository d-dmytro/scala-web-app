package engineering.dd.scalawebapp.auth

class RefreshTokenUtilsSuite extends munit.FunSuite {
  test("create token") {
    val token = RefreshTokenUtils.createToken
    assertEquals(token.length, 32)
  }
}
