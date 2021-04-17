package models.dtos.responses

import io.circe.Encoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import models.json.CirceImplicits

case class StatusResponseDTO(status: String = "ok")

object StatusResponseDTO extends CirceImplicits {
  implicit lazy val statusResponseDTOEncoder: Encoder[StatusResponseDTO] = deriveConfiguredEncoder
}
