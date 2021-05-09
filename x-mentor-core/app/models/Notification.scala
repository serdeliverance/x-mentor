package models

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import models.json.CirceImplicits

case class Notification(message: String)

object Notification extends CirceImplicits {
  implicit val encoder: Encoder[Notification] = deriveConfiguredEncoder
  implicit val decoder: Decoder[Notification] = deriveConfiguredDecoder
}
