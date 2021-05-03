package services.recommendations

import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.configurations.RecommendationConfig
import models.dtos.responses.RecommendationResponseDTO.InterestBaseRecommendationDTO
import models.{CourseNode, Student, Topic}
import play.api.Logging
import repositories.graph.{CourseRepository, TopicRepository}
import util.{ApplicationResultUtils, MapMarkerContext, RandomUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InterestRecommendationStrategy @Inject()(
    topicRepository: TopicRepository,
    resolver: RecommendationResolver,
    courseRepository: CourseRepository,
    recommendationConfig: RecommendationConfig
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils
    with ApplicationResultUtils {

  def recommend(
      student: Student
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Option[InterestBaseRecommendationDTO]] = {
    logger.info("Getting recommendation based on interest")
    for {
      interests          <- EitherT { topicRepository.getInterestTopicsByStudent(student.username) }
      selectedTopic      <- EitherT { takeRandomFromList[Topic](interests) }
      coursesToRecommend <- EitherT { resolver.getRecommendationBaseOnOtherStudentsByTopic(selectedTopic) }
      enrolledCourses    <- EitherT { courseRepository.getCoursesByStudentAndTopic(student, selectedTopic) }
      coursesToRecommend <- EitherT { difference[CourseNode](coursesToRecommend, enrolledCourses) }
      recommendation <- EitherT {
        handleResult(selectedTopic, coursesToRecommend.take(recommendationConfig.interestRecommendationSize))
      }
    } yield recommendation
  }.value

  private def handleResult(
      topic: Topic,
      courses: Seq[CourseNode]
    ): ApplicationResult[Option[InterestBaseRecommendationDTO]] =
    if (courses.nonEmpty) ApplicationResult(Some(InterestBaseRecommendationDTO(topic.name, courses)))
    else ApplicationResult(None)
}
