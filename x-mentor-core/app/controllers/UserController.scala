package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import models.dtos.requests.LoginRequestDTO
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.UserService
import util.MapMarkerContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
                                 val loginService: UserService,
                                 val controllerComponents: ControllerComponents
  )(implicit executionContext: ExecutionContext, markerContext: MapMarkerContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter {

  def login: Action[LoginRequestDTO] = Action.async(decode[LoginRequestDTO]) { implicit request =>
    val username = request.body.username
    val password = request.body.password

    loginService
      .login(username, password)
      .map {
        case Right(accessData) =>
          logger.info("Login success")
          Ok(accessData.asJson)
        case Left(error) =>
          logger.info("Login failed")
          handleError(error)
      }
  }

  def signup: Action[LoginRequestDTO] = Action.async(decode[LoginRequestDTO]) { implicit request =>
    val username = request.body.username
    val password = request.body.password

    loginService
      .signup(username, password)
      .map(_ => Created)
  }
}
