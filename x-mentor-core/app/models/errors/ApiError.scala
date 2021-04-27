package models.errors

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits

case class ApiError(errorMsg: String)

object ApiError extends CirceImplicits {

  implicit val apiErrorDecoder: Decoder[ApiError] = deriveConfiguredDecoder
  implicit val apiErrorEncoder: Encoder[ApiError] = deriveConfiguredEncoder
}
