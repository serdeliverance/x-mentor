package models.dtos.requests

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import models.json.CirceImplicits

case class StudentProgressDTO(durationInSeconds: Int)

object StudentProgressDTO extends CirceImplicits {
  implicit val registerWatchDtoEncoder: Encoder[StudentProgressDTO] = deriveConfiguredEncoder
  implicit val registerWatchDtoDecoder: Decoder[StudentProgressDTO] = deriveConfiguredDecoder
}
