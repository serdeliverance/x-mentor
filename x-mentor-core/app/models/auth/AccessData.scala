package models.auth

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits

case class AccessData(accessToken: String, idToken: String)

object AccessData extends CirceImplicits {
  implicit lazy val decoder: Decoder[AccessData] = deriveConfiguredDecoder[AccessData]
  implicit lazy val encoder: Encoder[AccessData] = deriveConfiguredEncoder[AccessData]
}
