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
import util.MapMarkerContext.fromAuthenticatedRequest

import javax.inject.{Inject, Singleton}
import util.MapMarkerContext

import scala.concurrent.{ExecutionContext, Future}

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

  def registerInterest(): Action[InterestRequestDTO] =
    authenticatedAction.async(decode[InterestRequestDTO]) { implicit request =>
      implicit val mmc: MapMarkerContext = fromAuthenticatedRequest()
      logger.info(s"Registering interests")
      interestService
        .registerInterest(request.student, request.body.topics.map(topic => Interest(request.student, topic)))
      Future(Ok)
    }

  def get(): Action[AnyContent] = authenticatedAction.async { implicit request =>
    implicit val mmc = fromAuthenticatedRequest()
    logger.info(s"Getting interests")
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
