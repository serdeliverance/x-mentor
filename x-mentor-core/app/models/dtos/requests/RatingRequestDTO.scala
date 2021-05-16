package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class RatingRequestDTO(course: String, stars: Int)

object RatingRequestDTO extends CirceImplicits {
  implicit lazy val ratingRequestDTODecoder: Decoder[RatingRequestDTO] = deriveConfiguredDecoder
}
