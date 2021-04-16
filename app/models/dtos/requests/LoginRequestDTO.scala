package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class LoginRequestDTO()

object LoginRequestDTO extends CirceImplicits {
  implicit lazy val loginRequestDTODecoder: Decoder[LoginRequestDTO] = deriveConfiguredDecoder
}