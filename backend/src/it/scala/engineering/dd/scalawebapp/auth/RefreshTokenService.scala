package engineering.dd.scalawebapp.auth

import engineering.dd.scalawebapp.IntegrationTestSuite

import java.util.UUID.randomUUID

val UUIDLength = 36

class RefreshTokenServiceSuite extends IntegrationTestSuite {
  test("create refresh token") {
    val userId = randomUUID.toString

    dependenciesFixture().refreshTokenService
      .createRefreshToken(userId)
      .map { result =>
        assert(!result.refreshToken.id.isEmpty)
        assertEquals(result.refreshToken.id.length, UUIDLength)
        assert(!result.refreshToken.tokenHash.isEmpty)
        assertEquals(result.refreshToken.userId, userId)
        assert(!result.originalToken.isEmpty)
        assertEquals(result.originalToken.length, 32)
      }
  }
}
