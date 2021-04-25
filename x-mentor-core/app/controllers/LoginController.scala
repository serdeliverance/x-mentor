package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import models.dtos.requests.LoginRequestDTO
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.LoginService
import util.MapMarkerContext

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.{Map => MMap}
import scala.concurrent.ExecutionContext

@Singleton
class LoginController @Inject()(
    val loginService: LoginService,
    val controllerComponents: ControllerComponents
  )(implicit executionContext: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter {

  def login: Action[LoginRequestDTO] = Action.async(decode[LoginRequestDTO]) { implicit request =>
    val username = request.body.username
    val password = request.body.password

    implicit val markerContext: MapMarkerContext = MapMarkerContext.fromRequest(
      MMap(MapMarkerContext.USERNAME -> username)
    )

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
}
