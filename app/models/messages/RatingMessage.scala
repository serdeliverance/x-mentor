package models.messages

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import models.json.CirceImplicits

case class RatingMessage(studentId: Long, courseId: Long, stars: Int)

object RatingMessage extends CirceImplicits {
  implicit lazy val ratingMessageDecoder: Decoder[RatingMessage] = deriveConfiguredDecoder
  implicit lazy val ratingMessageEncoder: Encoder[RatingMessage] = deriveConfiguredEncoder
}
