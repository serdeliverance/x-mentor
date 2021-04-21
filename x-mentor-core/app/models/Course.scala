package models

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits

case class Course(
    id: Option[Long] = None,
    title: String,
    description: String,
    content: String,
    preview: String,
    topic: String,
    rating: Option[Int] = None)

object Course extends CirceImplicits {
  implicit val encoder: Encoder[Course] = deriveConfiguredEncoder
  implicit val decoder: Decoder[Course] = deriveConfiguredDecoder
}
