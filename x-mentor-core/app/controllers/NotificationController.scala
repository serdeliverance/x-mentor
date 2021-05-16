package controllers

import akka.actor.ActorSystem
import play.api.Logging
import play.api.http.ContentTypes
import play.api.mvc._
import services.SseService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationController @Inject()(
    val controllerComponents: ControllerComponents,
    sseService: SseService
  )(implicit ec: ExecutionContext,
    system: ActorSystem)
    extends BaseController
    with Logging {

  def subscribeToNotifications(): Action[AnyContent] = Action {
    Ok.chunked(sseService.getSseSource)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

}
