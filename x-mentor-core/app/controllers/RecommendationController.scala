package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.RecommendationService
import util.MapMarkerContext.{fromAuthenticatedRequest, fromRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
    recommendationService: RecommendationService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def recommend(): Action[AnyContent] = authenticatedAction.async { implicit request =>
    implicit val mmc = fromAuthenticatedRequest()
    logger.info(s"Getting recommendations")
    recommendationService.getRecommendation(request.student).map {
      case Right(recommendations) =>
        logger.info("Recommendations retrieved successfully")
        Ok(recommendations.asJson)
      case Left(error) =>
        logger.info("Error getting recommendations")
        handleError(error)
    }
  }

  def visitorRecommendation(): Action[AnyContent] = Action.async { implicit request =>
    implicit val mmc = fromRequest()
    logger.info("Getting recommendation for visitor")
    recommendationService.getVisitorRecommendation().map {
      case Right(recommendations) =>
        logger.info("Recommendations retrieved successfully")
        Ok(recommendations.asJson)
      case Left(error) =>
        logger.info("Error getting recommendations")
        handleError(error)
    }
  }
}
