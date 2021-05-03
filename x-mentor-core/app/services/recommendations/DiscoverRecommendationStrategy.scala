package services.recommendations

import cats.data.EitherT
import cats.implicits._
import global.{ApplicationResult, ApplicationResultExtended}
import models.configurations.RecommendationConfig
import models.dtos.responses.RecommendationResponseDTO.DiscoverRecommendationDTO
import models.{CourseNode, Student, Topic}
import play.api.Logging
import repositories.graph.{CourseRepository, TopicRepository}
import util.{ApplicationResultUtils, MapMarkerContext, RandomUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DiscoverRecommendationStrategy @Inject()(
    topicRepository: TopicRepository,
    courseRepository: CourseRepository,
    recommendationConfig: RecommendationConfig
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils
    with ApplicationResultUtils {

  def recommend(
      student: Student
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Option[DiscoverRecommendationDTO]] = {
    logger.info("Getting recommendation suggesting topic related courses from catalog")

    getRandomTopicForStudent(student).innerFlatMap {
      case Some(topic) => getRecommendation(topic)
      case None        => ApplicationResult(None)
    }
  }

  def visitorRecommendation()(implicit mmc: MapMarkerContext): ApplicationResult[Option[DiscoverRecommendationDTO]] = {
    logger.info("Getting random topic recommendation for anonymous visitor")

    getRandomTopic().innerFlatMap {
      case Some(topic) => getRecommendation(topic)
      case None        => ApplicationResult(None)
    }
  }

  private def getRandomTopicForStudent(
      student: Student
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Option[Topic]] = {
    for {
      allTopics         <- EitherT { topicRepository.getTopics() }
      interests         <- EitherT { topicRepository.getInterestTopicsByStudent(student.username) }
      enrolledTopics    <- EitherT { topicRepository.getEnrolledTopics(student) }
      topicsToRecommend <- EitherT { getTopicsToRecommend(allTopics, interests, enrolledTopics) }
      selectedTopic     <- EitherT { takeRandomFromList[Topic](topicsToRecommend) }
    } yield selectedTopic
  }.value

  private def getRandomTopic()(implicit mmc: MapMarkerContext): ApplicationResult[Option[Topic]] =
    topicRepository.getTopics().innerFlatMap(topics => takeRandomFromList(topics))

  private def getRecommendation(topic: Topic): ApplicationResult[Option[DiscoverRecommendationDTO]] = {
    for {
      coursesToRecommend <- EitherT { courseRepository.getCoursesByTopic(topic) }
      recommendation <- EitherT {
        handleResult(topic, coursesToRecommend.take(recommendationConfig.discoveryRecommendationSize).distinct)
      }
    } yield recommendation
  }.value

  private def getTopicsToRecommend(
      allTopics: List[Topic],
      interests: List[Topic],
      enrolledTopics: List[Topic]
    ): ApplicationResult[List[Topic]] =
    ApplicationResult(allTopics.diff(interests).diff(enrolledTopics).distinct)

  private def handleResult(
      topic: Topic,
      courses: Seq[CourseNode]
    ): ApplicationResult[Option[DiscoverRecommendationDTO]] =
    if (courses.nonEmpty) ApplicationResult(Some(DiscoverRecommendationDTO(topic.name, courses)))
    else ApplicationResult(None)
}
