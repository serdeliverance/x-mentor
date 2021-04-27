package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class InterestRequestDTO(student: String, topics: List[String])

object InterestRequestDTO extends CirceImplicits {

  implicit val interestRequestDtoDecoder: Decoder[InterestRequestDTO] = deriveConfiguredDecoder
}
