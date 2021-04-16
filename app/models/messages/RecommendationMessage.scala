package models.messages

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits

case class RecommendationMessage(userId: Long, topicId: Option[Long], courseId: Option[Long])

object RecommendationMessage extends CirceImplicits {
  implicit lazy val recommendationMessageDecoder: Decoder[RecommendationMessage] = deriveConfiguredDecoder
  implicit lazy val recommendationMessageEncoder: Encoder[RecommendationMessage] = deriveConfiguredEncoder
}
