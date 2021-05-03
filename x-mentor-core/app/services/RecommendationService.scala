package services

import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.dtos.responses.RecommendationResponseDTO
import play.api.Logging
import repositories.graph.StudentRepository
import services.recommendations.{
  DiscoverRecommendationStrategy,
  EnrolledRecommendationStrategy,
  InterestRecommendationStrategy
}
import util.RandomUtils

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

  def getRecommendation(username: String): ApplicationResult[RecommendationResponseDTO] = {
    for {
      student                     <- EitherT { studentRepository.getStudent(username) }
      enrolledBasedRecommendation <- EitherT { enrolledRecommendationStrategy.recommend(student) }
      interestBasedRecommendation <- EitherT { interestRecommendationStrategy.recommend(student) }
      discoverRecommendation      <- EitherT { discoverRecommendationStrategy.recommend(student) }
    } yield RecommendationResponseDTO(enrolledBasedRecommendation, interestBasedRecommendation, discoverRecommendation)
  }.value
}