package models.dtos.requests

import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits

case class InterestRequestDTO(topics: List[String])

object InterestRequestDTO extends CirceImplicits {

  implicit val interestRequestDtoDecoder: Decoder[InterestRequestDTO] = deriveConfiguredDecoder
}
