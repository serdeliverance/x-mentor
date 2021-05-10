package controllers

import controllers.actions.AuthenticatedAction
import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import models.dtos.requests.StudentProgressDTO
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.MetricsService
import util.MapMarkerContext.fromAuthenticatedRequest

import javax.inject.{Inject, Singleton}
import util.MapMarkerContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MetricsController @Inject()(
    val controllerComponents: ControllerComponents,
    authenticatedAction: AuthenticatedAction,
    metricsService: MetricsService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with ErrorToResultConverter
    with Decodable
    with Logging {

  def registerStudentProgress(): Action[StudentProgressDTO] =
    authenticatedAction.async(decode[StudentProgressDTO]) { implicit request =>
      implicit val mmc: MapMarkerContext = fromAuthenticatedRequest()
      logger.info(s"Registering student progress")
      metricsService.registerStudentProgress(request.student, request.body.durationInSeconds)
      Future(Ok)
    }
}
