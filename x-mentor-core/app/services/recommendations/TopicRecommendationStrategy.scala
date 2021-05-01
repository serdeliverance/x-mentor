package services.recommendations

import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.configurations.RecommendationConfig
import models.dtos.responses.RecommendationResponseDTO.TopicBasedRecommendationDTO
import models.{CourseNode, Student, Topic}
import play.api.Logging
import repositories.graph.{CourseRepository, TopicRepository}
import util.{ApplicationResultUtils, RandomUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TopicRecommendationStrategy @Inject()(
    topicRepository: TopicRepository,
    courseRepository: CourseRepository,
    resolver: RecommendationResolver,
    recommendationConfig: RecommendationConfig
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils
    with ApplicationResultUtils {

  def recommendBasedOnEnrolledTopic(student: Student): ApplicationResult[Option[TopicBasedRecommendationDTO]] = {
    for {
      enrolledTopics     <- EitherT { topicRepository.getEnrolledTopics(student) }
      topic              <- EitherT { takeRandomFromList[Topic](enrolledTopics) }
      coursesRelated     <- EitherT { resolver.getRecommendationBaseOnOtherStudentsByTopic(topic) }
      enrolledCourses    <- EitherT { courseRepository.getCoursesByStudentAndTopic(student, topic) }
      coursesToRecommend <- EitherT { difference[CourseNode](coursesRelated, enrolledCourses) }
      recommendation <- EitherT {
        buildRecommendation(topic, coursesToRecommend)
      }
    } yield recommendation
  }.value

  private def buildRecommendation(
      topic: Topic,
      courses: Seq[CourseNode]
    ): ApplicationResult[Option[TopicBasedRecommendationDTO]] =
    if (courses.nonEmpty) ApplicationResult(Some(TopicBasedRecommendationDTO(topic.name, courses)))
    else ApplicationResult(None)
}
