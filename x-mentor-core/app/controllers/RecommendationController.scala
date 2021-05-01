package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.RecommendationService

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

  def recommend(): Action[AnyContent] = authenticatedAction.async { request =>
    logger.info(s"Getting recommendations for student: ${request.student}")
    recommendationService.getRecommendation(request.student).map {
      case Right(recommendations) =>
        logger.info("Recommendations retrieved successfully")
        Ok(recommendations.asJson)
      case Left(error) =>
        logger.info("Error getting recommendations")
        handleError(error)
    }
  }
}
