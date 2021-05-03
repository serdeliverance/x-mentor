package services.recommendations

import cats.data.EitherT
import global.ApplicationResult
import models.{CourseNode, Student, Topic}
import models.configurations.RecommendationConfig
import models.dtos.responses.RecommendationResponseDTO.{DiscoverRecommendationDTO, EnrolledBasedRecommendationDTO}
import play.api.Logging
import repositories.graph.{CourseRepository, TopicRepository}
import util.{ApplicationResultUtils, MapMarkerContext, RandomUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import cats.implicits._

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
    for {
      allTopics          <- EitherT { topicRepository.getTopics() }
      interests          <- EitherT { topicRepository.getInterestTopicsByStudent(student.username) }
      enrolledTopics     <- EitherT { topicRepository.getEnrolledTopics(student) }
      topicsToRecommend  <- EitherT { getTopicsToRecommend(allTopics, interests, enrolledTopics) }
      selectedTopic      <- EitherT { takeRandomFromList[Topic](topicsToRecommend) }
      coursesToRecommend <- EitherT { courseRepository.getCoursesByTopic(selectedTopic) }
      recommendation <- EitherT {
        handleResult(selectedTopic, coursesToRecommend.take(recommendationConfig.discoveryRecommendationSize).distinct)
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
