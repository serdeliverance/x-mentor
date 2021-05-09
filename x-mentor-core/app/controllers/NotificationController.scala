package controllers

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{CompletionStrategy, OverflowStrategy}
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import play.api.mvc._
import services.SseService

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class NotificationController @Inject()(
    val controllerComponents: ControllerComponents,
    sseService: SseService
  )(implicit ec: ExecutionContext,
    system: ActorSystem)
    extends BaseController
    with Logging {

  def subscribeToNotifications(): Action[AnyContent] = Action {
    Ok.chunked(sseService.getSseSource())
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

}
