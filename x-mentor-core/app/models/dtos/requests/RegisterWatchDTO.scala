package models.dtos.requests

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import models.json.CirceImplicits

case class RegisterWatchDTO(durationInMinutes: Int)

object RegisterWatchDTO extends CirceImplicits {
  implicit val registerWatchDtoEncoder: Encoder[RegisterWatchDTO] = deriveConfiguredEncoder
  implicit val registerWatchDtoDecoder: Decoder[RegisterWatchDTO] = deriveConfiguredDecoder
}
