package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import models.Rating
import models.dtos.requests.RatingRequestDTO
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.RatingService
import util.MapMarkerContext.fromAuthenticatedRequest
import javax.inject.{Inject, Singleton}
import util.MapMarkerContext

import scala.concurrent.ExecutionContext

@Singleton
class RatingController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
    ratingService: RatingService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def rate(): Action[RatingRequestDTO] = authenticatedAction.async(decode[RatingRequestDTO]) { implicit request =>
    implicit val mmc: MapMarkerContext = fromAuthenticatedRequest()
    val rating       = Rating(student = request.student, course = request.body.course, stars = request.body.stars)
    logger.info(s"Rating course: ${rating.course}")
    ratingService
      .rate(rating)
      .map {
        case Right(_) =>
          logger.info(s"Course ${rating.course} rated successfully")
          Ok
        case Left(error) =>
          logger.info(s"Error rating course ${rating.course}")
          handleError(error)
      }
  }
}
