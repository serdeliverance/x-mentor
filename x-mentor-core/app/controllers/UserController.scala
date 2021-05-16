package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import models.dtos.requests.LoginRequestDTO
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.UserService
import util.MapMarkerContext
import scala.collection.mutable.{Map => MMap}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
    loginService: UserService,
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
          Ok(accessData.asJson)
        case Left(error) =>
          handleError(error)
      }
  }

  def signup: Action[LoginRequestDTO] = Action.async(decode[LoginRequestDTO]) { implicit request =>
    val username = request.body.username
    val password = request.body.password

    implicit val markerContext: MapMarkerContext = MapMarkerContext.fromRequest(
      MMap(MapMarkerContext.USERNAME -> username)
    )

    loginService
      .signup(username, password)
      .map {
        case Right(_) =>
          Created
        case Left(error) =>
          handleError(error)
      }
  }
}
