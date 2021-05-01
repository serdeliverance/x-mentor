package services

import global.ApplicationResult
import models.CourseNode
import play.api.Logging
import services.recommendations.RecommendationTopicStrategy
import util.RandomUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationService @Inject()(
    recommendationTopicStrategy: RecommendationTopicStrategy
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils {

  def getRecommendation(student: String): ApplicationResult[Seq[CourseNode]] =
    recommendationTopicStrategy
      .recommendationBasedOnEnrolledTopic(student)
//      .recoverWith { _ =>
//        logger.info("fallback strategy one: get recommendation based on interests")
//        recommendationBasedOnInterest(student)
//      }
//      .recoverWith { _ =>
//        logger.info("fallback strategy two: get recommendation based on random topic")
//        recommendationBasedOnRandomTopic(student)
//      }
//      .recover { _ =>
//        logger.info("no recommendations could be found. Return empty response")
//        Right(List.empty[CourseNode])
//      }

  def recommendationBasedOnInterest(student: String): ApplicationResult[List[CourseNode]] = ???

  def recommendationBasedOnRandomTopic(student: String): ApplicationResult[List[CourseNode]] = ???
}
