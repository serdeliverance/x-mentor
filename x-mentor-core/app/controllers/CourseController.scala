package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter

import javax.inject.{Inject, Singleton}
import models.Course
import models.dtos.requests.{CourseCreationRequestDTO, CourseEnrollmentRequestDTO}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.CourseService

import io.circe.syntax._

import scala.concurrent.ExecutionContext

@Singleton
class CourseController @Inject()(
    val controllerComponents: ControllerComponents,
    courseService: CourseService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def create(): Action[CourseCreationRequestDTO] = Action.async(decode[CourseCreationRequestDTO]) { request =>
    logger.info(s"Creating course")
    val course = Course(title = request.body.title,
                        description = request.body.description,
                        content = request.body.content,
                        preview = request.body.preview,
                        topic = request.body.topic)
    courseService
      .create(course)
      .map(_ => Created)
  }

  def enroll(courseId: Long): Action[CourseEnrollmentRequestDTO] = Action.async(decode[CourseEnrollmentRequestDTO]) {
    request =>
      logger.info(s"Enroll in course $courseId")
      courseService
        .enroll(courseId)
        .map(_ => Ok)
  }

  def retrieveAll(): Action[AnyContent] = Action.async{ _ =>
    logger.info(s"Retrieving all courses")
    courseService
      .retrieveAll()
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
}
