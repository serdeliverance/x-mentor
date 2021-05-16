package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import io.circe.syntax._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.TopicService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TopicController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
    topicService: TopicService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Logging
    with Decodable
    with ErrorToResultConverter {

  def getAll(): Action[AnyContent] = authenticatedAction.async { _ =>
    logger.info("Getting all topics")
    topicService.getAll().map {
      case Right(topics) =>
        logger.info("Topics retrieved successfully")
        Ok(topics.asJson)
      case Left(error) =>
        logger.info("Error retrieving topics")
        handleError(error)
    }
  }
}
