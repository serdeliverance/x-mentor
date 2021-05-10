package models.dtos.responses

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.StudentProgress
import models.json.CirceImplicits

case class LeadersBoardDTO(list: Seq[StudentProgress])

object LeadersBoardDTO extends CirceImplicits {

  implicit val leadersBoardDtoEncoder: Encoder[LeadersBoardDTO] = deriveConfiguredEncoder
  implicit val leadersBoardDecoder: Decoder[LeadersBoardDTO]    = deriveConfiguredDecoder
}
