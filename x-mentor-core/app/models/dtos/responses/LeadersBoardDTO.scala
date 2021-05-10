package models.dtos.responses

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.dtos.responses.LeadersBoardDTO.LeaderDTO
import models.json.CirceImplicits

case class LeadersBoardDTO(list: List[LeaderDTO])

object LeadersBoardDTO extends CirceImplicits {

  case class LeaderDTO(student: String, hours: Double)

  implicit val leaderDtoEncoder: Encoder[LeaderDTO] = deriveConfiguredEncoder
  implicit val leaderDtoDecoder: Decoder[LeaderDTO] = deriveConfiguredDecoder

  implicit val leadersBoardDtoEncoder: Encoder[LeadersBoardDTO] = deriveConfiguredEncoder
  implicit val leadersBoardDecoder: Decoder[LeadersBoardDTO]    = deriveConfiguredDecoder
}
