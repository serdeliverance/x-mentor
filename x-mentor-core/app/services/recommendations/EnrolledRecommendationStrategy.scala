package services.recommendations

import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.configurations.RecommendationConfig
import models.dtos.responses.RecommendationResponseDTO.EnrolledBasedRecommendationDTO
import models.{CourseNode, Student}
import play.api.Logging
import repositories.graph.{CourseRepository, StudentRepository, TopicRepository}
import util.{ApplicationResultUtils, MapMarkerContext, RandomUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EnrolledRecommendationStrategy @Inject()(
    topicRepository: TopicRepository,
    courseRepository: CourseRepository,
    studentRepository: StudentRepository,
    recommendationConfig: RecommendationConfig
  )(implicit ec: ExecutionContext)
    extends Logging
    with RandomUtils
    with ApplicationResultUtils {

  def recommend(
      student: Student
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Option[EnrolledBasedRecommendationDTO]] = {
    logger.info("Getting recommendation based on students that has taken the same course")
    for {
      courses                      <- EitherT { courseRepository.getCoursesByStudent(student.username) }
      selectedCourse               <- EitherT { takeRandomFromList[CourseNode](courses) }
      selectedCourseTopic          <- EitherT { topicRepository.getTopicByCourse(selectedCourse) }
      studentsEnrolledToSameCourse <- EitherT { studentRepository.getStudentByCourse(selectedCourse) }
      similarCourses <- EitherT {
        courseRepository.getCoursesByStudentAndTopicInBulk(studentsEnrolledToSameCourse, selectedCourseTopic)
      }
      coursesToRecommend <- EitherT { difference(similarCourses, Seq(selectedCourse)) }
      recommendation <- EitherT {
        handleResult(selectedCourse, coursesToRecommend.take(recommendationConfig.enrolledRecommendationSize))
      }
    } yield recommendation
  }.value

  private def handleResult(
      reason: CourseNode,
      courses: Seq[CourseNode]
    ): ApplicationResult[Option[EnrolledBasedRecommendationDTO]] =
    if (courses.nonEmpty) ApplicationResult(Some(EnrolledBasedRecommendationDTO(reason.name, courses)))
    else ApplicationResult(None)
}
