package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import javax.inject.{Inject, Singleton}
import models.dtos.requests.LoginRequestDTO
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.CourseService

import scala.concurrent.ExecutionContext

@Singleton
class LoginController @Inject()(
    val controllerComponents: ControllerComponents,
    courseService: CourseService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def login(): Action[LoginRequestDTO] = Action.async(decode[LoginRequestDTO]) { request =>
    logger.info(s"Creating course")
    ???
  }
}
