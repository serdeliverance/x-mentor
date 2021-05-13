package services

import cats.data.EitherT
import cats.implicits._
import global.{ApplicationResult, ApplicationResultExtended}
import models.dtos.responses.RecommendationResponseDTO
import models.dtos.responses.RecommendationResponseDTO.visitorRecommendation
import play.api.Logging
import repositories.graph.StudentRepository
import services.recommendations.{
  DiscoverRecommendationStrategy,
  EnrolledRecommendationStrategy,
  InterestRecommendationStrategy
}
import util.{MapMarkerContext, RandomUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationService @Inject()(
    studentRepository: StudentRepository,
    enrolledRecommendationStrategy: EnrolledRecommendationStrategy,
    discoverRecommendationStrategy: DiscoverRecommendationStrategy,
    interestRecommendationStrategy: InterestRecommendationStrategy
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils {

  /**
    * Performs different recommendation strategies and aggregate its results.
    * These are the following:
    *    1. Recommend based on a course that the student enrolled in
    *    2. Recommend based on an interest the student has shown
    *    3. Recommend based on a random course the platform has
    */
  def getRecommendation(
      username: String
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[RecommendationResponseDTO] = {
    for {
      student                     <- EitherT { studentRepository.getStudent(username) }
      enrolledBasedRecommendation <- EitherT { enrolledRecommendationStrategy.recommend(student) }
      interestBasedRecommendation <- EitherT { interestRecommendationStrategy.recommend(student) }
      discoverRecommendation      <- EitherT { discoverRecommendationStrategy.recommend(student) }
    } yield RecommendationResponseDTO(enrolledBasedRecommendation, interestBasedRecommendation, discoverRecommendation)
  }.value

  /**
    * Retrieves recommendation for non-logged users. For that case, it uses the [[DiscoverRecommendationStrategy]]
    */
  def getVisitorRecommendation()(implicit mmc: MapMarkerContext): ApplicationResult[RecommendationResponseDTO] =
    discoverRecommendationStrategy
      .visitorRecommendation()
      .innerMap(recommendation => Right(visitorRecommendation(recommendation)))
}
