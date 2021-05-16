package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import javax.inject.{Inject, Singleton}
import models.Course
import models.dtos.requests.{CourseCreationRequestDTO, CourseEnrollmentRequestDTO}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.CourseService
import io.circe.syntax._
import util.MapMarkerContext
import util.MapMarkerContext.fromAuthenticatedRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CourseController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
    courseService: CourseService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def create(): Action[CourseCreationRequestDTO] = authenticatedAction.async(decode[CourseCreationRequestDTO]) {
    implicit request =>
      implicit val mmc: MapMarkerContext = fromAuthenticatedRequest()
      logger.info(s"Creating course")
      val course = Course(title = request.body.title,
                          description = request.body.description,
                          content = request.body.content,
                          preview = request.body.preview,
                          topic = request.body.topic)
      courseService
        .create(course)

      Future(Created)
  }

  def enroll(courseId: Long): Action[CourseEnrollmentRequestDTO] =
    authenticatedAction.async(decode[CourseEnrollmentRequestDTO]) { request =>
      logger.info(s"Enroll in course $courseId")
      courseService
        .enroll(courseId, request.student)
        .map(_ => Ok)
    }

  def retrieve(q: String, page: Int): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Retrieving courses")
    courseService
      .retrieve(q, page)
      .map {
        case Right(courses) =>
          logger.info("Courses retrieved successfully")
          Ok(courses.asJson)
        case Left(error) =>
          logger.info("Error retrieving courses")
          handleError(error)
      }
  }

  def retrieveById(courseId: Long): Action[AnyContent] =
    Action.async { _ =>
      logger.info(s"Retrieve course $courseId")
      courseService
        .retrieveById(courseId)
        .map {
          case Right(course) =>
            logger.info(s"Course with id: $courseId retrieved successfully")
            Ok(course.asJson)
          case Left(error) =>
            logger.info(s"Error getting course: $courseId")
            handleError(error)
        }
    }

  def getByStudent(page: Int): Action[AnyContent] = authenticatedAction.async { request =>
    val student = request.student
    logger.info(s"Retrieving courses by student: $student")
    courseService
      .getCoursesByStudent(student, page)
      .map {
        case Right(course) =>
          logger.info(s"Courses retrieved successfully")
          Ok(course.asJson)
        case Left(error) =>
          logger.info(s"Error retrieving courses by student: $student")
          handleError(error)
      }
  }
}
