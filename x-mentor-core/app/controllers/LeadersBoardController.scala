package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.LeadersBoardService
import util.MapMarkerContext.fromAuthenticatedRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LeadersBoardController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
    leadersBoardService: LeadersBoardService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with ErrorToResultConverter
    with Decodable
    with Logging {

  def get(): Action[AnyContent] = authenticatedAction.async { implicit request =>
    implicit val mmc = fromAuthenticatedRequest()
    logger.info("Getting leadersboard")
    leadersBoardService.get().map {
      case Right(leadersBoard) =>
        logger.info("Leadersboard data retrieved successfully")
        Ok(leadersBoard.asJson)
      case Left(error) =>
        logger.info("Error getting leadersboard data")
        handleError(error)
    }
  }
}
