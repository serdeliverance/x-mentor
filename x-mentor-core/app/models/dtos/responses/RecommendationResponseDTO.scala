package models.dtos.responses

import io.circe.Encoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import models.CourseNode
import models.dtos.responses.RecommendationResponseDTO.{
  DiscoverRecommendationDTO,
  EnrolledBasedRecommendationDTO,
  InterestBaseRecommendationDTO
}
import models.json.CirceImplicits

case class RecommendationResponseDTO(
    basedOnEnrolled: Option[EnrolledBasedRecommendationDTO],
    basedOnInterest: Option[InterestBaseRecommendationDTO],
    discover: Option[DiscoverRecommendationDTO])

object RecommendationResponseDTO extends CirceImplicits {

  def visitorRecommendation(discoverRecommendationDTO: Option[DiscoverRecommendationDTO]): RecommendationResponseDTO =
    RecommendationResponseDTO(None, None, discoverRecommendationDTO)

  case class EnrolledBasedRecommendationDTO(reason: String, courses: Seq[CourseNode])

  case class InterestBaseRecommendationDTO(topic: String, courses: Seq[CourseNode])

  case class DiscoverRecommendationDTO(topic: String, courses: Seq[CourseNode])

  case class TopicBasedRecommendationDTO(topic: String, courses: Seq[CourseNode])

  implicit val recommendationResponseDTOEncoder: Encoder[RecommendationResponseDTO] = deriveConfiguredEncoder

  implicit val enrolledBasedRecommendationDTOEncoder: Encoder[EnrolledBasedRecommendationDTO] = deriveConfiguredEncoder

  implicit val interestBaseRecommendationDTOEncoder: Encoder[InterestBaseRecommendationDTO] = deriveConfiguredEncoder

  implicit val discoverRecommendationDTOEncoder: Encoder[DiscoverRecommendationDTO] = deriveConfiguredEncoder
}
