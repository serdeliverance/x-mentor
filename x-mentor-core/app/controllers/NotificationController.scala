package controllers

import akka.NotUsed
import akka.stream.scaladsl.Source
import javax.inject.{Inject, Singleton}
import models.Notification
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.EventSource
import services.NotificationService
import play.api.mvc._
import io.circe.syntax._

@Singleton
class NotificationController @Inject()(
    val controllerComponents: ControllerComponents,
    notificationService: NotificationService)
    extends BaseController
    with Logging {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def subscribeToNotifications() = Action {

    //val sourceOfSessionStatuses: Source[String, NotUsed] = notificationService.getSourceOfNotifications()
    //.map(notification => notification.message)

    val source = Source.apply(List(Notification("kiki").asJson.noSpaces, Notification("foo").asJson.noSpaces, Notification("bar").asJson.noSpaces))

    Ok.chunked(source via EventSource.flow)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

  def registerNotification() = Action {
    logger.info("Registering new notification")
    Ok
  }

}
