package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class LoginRequestDTO(username: String, password: String)

object LoginRequestDTO extends CirceImplicits {
  implicit lazy val loginRequestDTODecoder: Decoder[LoginRequestDTO] = deriveConfiguredDecoder
}
