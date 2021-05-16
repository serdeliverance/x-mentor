package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.LeaderboardService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LeaderboardController @Inject()(
    val controllerComponents: ControllerComponents,
    leadersBoardService: LeaderboardService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with ErrorToResultConverter
    with Decodable
    with Logging {

  def get(): Action[AnyContent] = Action.async { implicit request =>
    logger.info("Getting leaderboard")
    leadersBoardService.get().map {
      case Right(leadersBoard) =>
        logger.info("Leaderboard data retrieved successfully")
        Ok(leadersBoard.asJson)
      case Left(error) =>
        logger.info("Error getting leaderboard data")
        handleError(error)
    }
  }
}
