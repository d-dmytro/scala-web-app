package engineering.dd.scalawebapp.utils

import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*

case class ErrorBody(messages: List[String])

object ErrorResponse {
  def getErrorResponse(messages: List[String]): Json = {
    ErrorBody(messages).asJson
  }

  def getErrorResponse(message: String): Json = {
    ErrorBody(List(message)).asJson
  }
}
