package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import models.Interest
import models.dtos.requests.InterestRequestDTO
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.InterestService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InterestController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
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

  def getByStudent(): Action[AnyContent] = authenticatedAction.async { request =>
    logger.info(s"Getting interests of student: ${request.student}")
    interestService.getInterests(request.student).map {
      case Right(topics) =>
        logger.info("Interests retrieved successfully")
        Ok(topics.asJson)
      case Left(error) =>
        logger.info(s"Error retrieving interests for student: ${request.student}")
        handleError(error)
    }
  }

}
