package models

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import models.json.CirceImplicits

case class CourseResponse(total: Long, courses: Seq[Course])

object CourseResponse extends CirceImplicits {
  implicit val encoder: Encoder[CourseResponse] = deriveConfiguredEncoder
  implicit val decoder: Decoder[CourseResponse] = deriveConfiguredDecoder
}
