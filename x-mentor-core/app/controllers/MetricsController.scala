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
import scala.concurrent.ExecutionContext

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
      implicit val mmc = fromAuthenticatedRequest()
      logger.info("Registering watching time")
      metricsService.registerStudentProgress(request.student, request.body.durationInSeconds).map(_ => Ok)
    }
}
