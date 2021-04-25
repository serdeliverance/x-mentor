package models.auth

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import models.json.CirceImplicits

case class AuthErrorResponse(error: String, errorDescription: String)

object AuthErrorResponse extends CirceImplicits {
  implicit val decoder: Decoder[AuthErrorResponse] = deriveConfiguredDecoder
  implicit val encoder: Encoder[AuthErrorResponse] = deriveConfiguredEncoder
}
