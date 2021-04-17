package models.validations

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits

case class ValidationObject(errorType: String, validationErrors: List[ValidationError])

object ValidationObject extends CirceImplicits {
  implicit val decoder: Decoder[ValidationObject] = deriveDecoder[ValidationObject]
  implicit val encoder: Encoder[ValidationObject] = deriveEncoder[ValidationObject]
}
