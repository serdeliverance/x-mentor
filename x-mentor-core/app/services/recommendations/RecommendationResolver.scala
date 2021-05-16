package services.recommendations

import cats.data.EitherT
import global.ApplicationResult
import models.{CourseNode, Topic}
import play.api.Logging
import repositories.graph.{CourseRepository, StudentRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import cats.implicits._
import util.MapMarkerContext

@Singleton
class RecommendationResolver @Inject()(
    studentRepository: StudentRepository,
    courseRepository: CourseRepository
  )(implicit ec: ExecutionContext)
    extends Logging {

  def getRecommendationBaseOnOtherStudentsByTopic(
      topic: Topic
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Seq[CourseNode]] = {
    logger.info(s"Looking for recommendation based on others students enrolled to courses of topic: ${topic.name}")
    for {
      students <- EitherT { studentRepository.getStudentsByTopic(topic) }
      courses  <- EitherT { courseRepository.getCoursesByStudentAndTopicInBulk(students, topic) }
    } yield courses.distinct
  }.value
}
