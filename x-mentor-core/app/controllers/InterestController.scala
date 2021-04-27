package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import models.dtos.requests.InterestRequestDTO
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.InterestService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InterestController @Inject()(
    val controllerComponents: ControllerComponents,
    interestService: InterestService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with ErrorToResultConverter
    with Decodable
    with Logging {

  def interest(): Action[InterestRequestDTO] = Action.async(decode[InterestRequestDTO]) { request =>
    logger.info(s"Registering ${request.body.student} interests")
    interestService.interest(request.body.student, request.body.interests).map {
      case Right(_) =>
        logger.info("Interest registered successfully")
        Ok
      case Left(error) =>
        logger.info("Error registering interest")
        handleError(error)
    }
  }

}