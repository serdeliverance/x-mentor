package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class CourseCreationRequestDTO()

object CourseCreationRequestDTO extends CirceImplicits {
  implicit lazy val courseCreationRequestDTODecoder: Decoder[CourseCreationRequestDTO] = deriveConfiguredDecoder
}
