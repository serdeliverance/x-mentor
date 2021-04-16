package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class CourseRecommendationRequestDTO(courseId: Long)

object CourseRecommendationRequestDTO extends CirceImplicits {
  implicit lazy val courseRecommendationRequestDTODecoder: Decoder[CourseRecommendationRequestDTO] =
    deriveConfiguredDecoder
}
