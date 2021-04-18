package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import models.Rating
import models.dtos.requests.RatingRequestDTO
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.RatingService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RatingController @Inject()(
    val controllerComponents: ControllerComponents,
    ratingService: RatingService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def rate(): Action[RatingRequestDTO] = Action.async(decode[RatingRequestDTO]) { request =>
    // TODO remove this when action refined were implemented (this action validates user and creates rating object)
    val rating = Rating(student = request.body.student, course = request.body.course, stars = request.body.stars)
    logger.info(s"Rating course: ${rating.course}")
    ratingService
      .rate(rating)
      .map(_ => Ok)
  }
}
