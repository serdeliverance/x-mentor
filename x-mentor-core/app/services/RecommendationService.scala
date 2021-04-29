package services

import cats.data.EitherT
import global.ApplicationResult
import models.Course
import play.api.Logging
import repositories.RedisGraphRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationService @Inject()(redisGraphRepository: RedisGraphRepository)(implicit ec: ExecutionContext)
    extends Logging {

  def getRecommendation(student: String): ApplicationResult[List[Course]] =
    recommendationBasedOnRandomTopic(student)
      .recoverWith { _ =>
        logger.info("fallback strategy one: get recommendation based on interests")
        recommendationBasedOnInterest(student)
      }
      .recoverWith { _ =>
        logger.info("fallback strategy two: get recommendation based on random topic")
        recommendationBasedOnRandomTopic(student)
      }
      .recover { _ =>
        logger.info("no recommendations could be found. Return empty response")
        Right(List.empty[Course])
      }

  def recommendationBasedOnEnrolledTopic(student: String): ApplicationResult[List[Course]] =
    for {
      enrolledTopics  <- EitherT { redisGraphRepository.getEnrolledTopics(student) }
      topic           <- EitherT { getRandomTopicOrSuggestOne(enrolledTopics) }
      courses         <- EitherT { getRecommendationBaseOnOtherStudentsOrSuggestOne(topic) }
      filteredCourses <- EitherT { checkInBlooms(courses) }
      _               <- EitherT { validateResult(filteredCourses) } // TODO if no result => future failed (in order to use recover)
    } yield List.empty

  def recommendationBasedOnInterest(student: String): ApplicationResult[List[Course]] = ???

  def recommendationBasedOnRandomTopic(student: String): ApplicationResult[List[Course]] = ???
}
