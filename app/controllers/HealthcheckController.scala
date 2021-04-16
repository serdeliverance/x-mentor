package controllers

import controllers.circe.Decodable
import io.circe.syntax._
import models.dtos.responses.StatusResponseDTO
import models.json.CirceImplicits
import play.api.mvc._

import javax.inject._

@Singleton
class HealthcheckController @Inject()(val controllerComponents: ControllerComponents)
    extends BaseController
    with Decodable
    with CirceImplicits {

  def healthcheck() = Action { implicit request: Request[AnyContent] =>
    Ok(StatusResponseDTO().asJson)
  }
}
