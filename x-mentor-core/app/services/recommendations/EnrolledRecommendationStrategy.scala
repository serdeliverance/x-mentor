package services.recommendations

import cats.data.EitherT
import cats.implicits._
import global.{ApplicationResult, ApplicationResultExtended}
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
    logger
      .info("Getting recommendation based on students that has taken the same course")
    selectRandomCourse(student).innerFlatMap {
      case Some(selectedCourse) => getRecommendation(selectedCourse)
      case None                 => ApplicationResult(None)
    }
  }

  private def selectRandomCourse(student: Student): ApplicationResult[Option[CourseNode]] =
    courseRepository
      .getCoursesByStudent(student.username)
      .innerFlatMap { courses =>
        takeRandomFromList[CourseNode](courses)
      }

  private def getRecommendation(course: CourseNode): ApplicationResult[Option[EnrolledBasedRecommendationDTO]] = {
    for {
      selectedCourseTopic          <- EitherT { topicRepository.getTopicByCourse(course) }
      studentsEnrolledToSameCourse <- EitherT { studentRepository.getStudentByCourse(course) }
      similarCourses <- EitherT {
        courseRepository.getCoursesByStudentAndTopicInBulk(studentsEnrolledToSameCourse, selectedCourseTopic)
      }
      coursesToRecommend <- EitherT { difference(similarCourses, Seq(course)) }
      recommendation <- EitherT {
        handleResult(course, coursesToRecommend.take(recommendationConfig.enrolledRecommendationSize))
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
