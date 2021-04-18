package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import play.api.Logging
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.TopicService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import io.circe.syntax._
import repositories.RedisGraphRepository

@Singleton
class TopicController @Inject()(
    val controllerComponents: ControllerComponents,
    topicService: TopicService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Logging
    with Decodable
    with ErrorToResultConverter {

  def getAll(): Action[AnyContent] = Action.async { _ =>
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
