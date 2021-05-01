package services.recommendations

import akka.Done
import akka.Done.done
import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.configurations.RecommendationConfig
import models.{CourseNode, Topic}
import play.api.Logging
import repositories.graph.{CourseRepository, StudentRepository, TopicRepository}
import util.RandomUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RecommendationTopicStrategy @Inject()(
    topicRepository: TopicRepository,
    studentRepository: StudentRepository,
    courseRepository: CourseRepository,
    recommendationConfig: RecommendationConfig
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils {

  def recommendationBasedOnEnrolledTopic(studentUsername: String): ApplicationResult[Seq[CourseNode]] = {
    for {
      student         <- EitherT { studentRepository.getStudent(studentUsername) }
      enrolledTopics  <- EitherT { topicRepository.getEnrolledTopics(student) }
      topic           <- EitherT { getRandomTopic(enrolledTopics) }
      courses         <- EitherT { getRecommendationBaseOnOtherStudentsOrSuggestOne(topic) }
      enrolledCourses <- EitherT { courseRepository.getCoursesByStudentAndTopic(student, topic) }
      recommendations <- EitherT { difference(courses, enrolledCourses) }
      _               <- EitherT { validateResult(recommendations.take(recommendationConfig.coursesToRecommend)) }
    } yield recommendations
  }.value

  private def difference(
      courses: Seq[CourseNode],
      studentCourses: Seq[CourseNode]
    ): ApplicationResult[Seq[CourseNode]] =
    ApplicationResult {
      courses.diff(studentCourses)
    }

  private def getRandomTopic(enrolledTopics: List[Topic]): ApplicationResult[Topic] =
    ApplicationResult(enrolledTopics(randomInt(enrolledTopics.length - 1)))

  private def getRecommendationBaseOnOtherStudentsOrSuggestOne(topic: Topic): ApplicationResult[Seq[CourseNode]] = {
    logger.info(s"Looking for recommendation based on others students enrolled to $topic related courses")
    for {
      students <- EitherT { studentRepository.getStudentsByTopic(topic) }
      courses  <- EitherT { courseRepository.getCoursesByStudentAndTopicInBulk(students, topic) }
    } yield courses.distinct
  }.value

  private def validateResult(filteredCourses: Seq[CourseNode]): ApplicationResult[Done] =
    if (filteredCourses.nonEmpty) ApplicationResult(done())
    else Future.failed(new IllegalArgumentException("No courses to suggest for topic"))
}
