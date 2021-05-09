package controllers

import akka.actor.ActorSystem
import models.configurations.SSEConfiguration
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationController @Inject()(
    val controllerComponents: ControllerComponents,
    sseConfiguration: SSEConfiguration
  )(implicit ec: ExecutionContext,
    system: ActorSystem)
    extends BaseController
    with Logging {

  sealed trait NotificationMessage
  case class CourseCreated(course: String, createdAt: LocalDateTime) extends NotificationMessage

  def subscribeToNotifications(): Action[AnyContent] = Action {
    Ok.chunked(sseConfiguration.sseSource via EventSource.flow)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

}
