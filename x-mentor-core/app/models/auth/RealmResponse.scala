package models.auth

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits

case class RealmResponse(publicKey: String)

object RealmResponse extends CirceImplicits {
  implicit lazy val decoder: Decoder[RealmResponse] = deriveConfiguredDecoder[RealmResponse]
  implicit lazy val encoder: Encoder[RealmResponse] = deriveConfiguredEncoder[RealmResponse]
}
