package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class CourseEnrollmentRequestDTO()

object CourseEnrollmentRequestDTO extends CirceImplicits {
  implicit lazy val courseEnrollmentRequestDTODecoder: Decoder[CourseEnrollmentRequestDTO] = deriveConfiguredDecoder
}
