package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import javax.inject.{Inject, Singleton}
import models.dtos.requests.{CourseCreationRequestDTO, CourseEnrollmentRequestDTO}
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.CourseService

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
    courseService
      .create()
      .map(_ => Ok)
  }

  def enroll(courseId: Long): Action[CourseEnrollmentRequestDTO] = Action.async(decode[CourseEnrollmentRequestDTO]) { request =>
    logger.info(s"Enroll in course ${courseId}")
    courseService
      .enroll()
      .map(_ => Ok)
  }

  def retrieveAll(): Action[CourseEnrollmentRequestDTO] = Action.async(decode[CourseEnrollmentRequestDTO]) { request =>
    logger.info(s"Retrieving all courses")
    courseService
      .enroll()
      .map(_ => Ok)
  }

  def retrieveById(courseId: Long): Action[CourseEnrollmentRequestDTO] = Action.async(decode[CourseEnrollmentRequestDTO]) { request =>
    logger.info(s"Retrieve course ${courseId}")
    courseService
      .enroll()
      .map(_ => Ok)
  }
}
