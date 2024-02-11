package engineering.dd.scalawebapp.auth.refreshtoken

case class RefreshToken(
    id: String,
    userId: String,
    tokenHash: String,
    fingerprint: String
)
