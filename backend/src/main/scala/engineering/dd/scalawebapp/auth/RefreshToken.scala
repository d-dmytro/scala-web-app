package engineering.dd.scalawebapp.auth

case class RefreshToken(
    id: String,
    userId: String,
    tokenHash: String,
    fingerprint: String
)
