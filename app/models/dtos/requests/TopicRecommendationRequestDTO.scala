package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class TopicRecommendationRequestDTO(topicId: Long)

object TopicRecommendationRequestDTO extends CirceImplicits {
  implicit lazy val topicRecommendationRequestDTODecoder: Decoder[TopicRecommendationRequestDTO] =
    deriveConfiguredDecoder
}
