package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import models.Interest
import models.dtos.requests.InterestRequestDTO
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
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

  def registerInterest(): Action[InterestRequestDTO] = Action.async(decode[InterestRequestDTO]) { request =>
    logger.info(s"Registering ${request.body.student} interests")
    interestService
      .registerInterest(request.body.student, request.body.topics.map(topic => Interest(request.body.student, topic)))
      .map {
        case Right(_) =>
          logger.info("Interest registered successfully")
          Ok
        case Left(error) =>
          logger.info("Error registering interest")
          handleError(error)
      }
  }

}
