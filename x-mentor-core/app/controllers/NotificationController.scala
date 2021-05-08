package controllers

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source

import javax.inject.{Inject, Singleton}
import models.Notification
import play.api.Logging
import play.api.http.ContentTypes
import play.api.libs.EventSource
import services.NotificationService
import play.api.mvc._
import io.circe.syntax._

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

@Singleton
class NotificationController @Inject()(
    val controllerComponents: ControllerComponents,
    notificationService: NotificationService
  )(implicit ec: ExecutionContext,
    system: ActorSystem)
    extends BaseController
    with Logging {

  sealed trait NotificationMessage
  case class CourseCreated(course: String, createdAt: LocalDateTime) extends NotificationMessage

  def subscribeToNotifications() = Action {

    //val sourceOfSessionStatuses: Source[String, NotUsed] = notificationService.getSourceOfNotifications()
    //.map(notification => notification.message)

    val (sseActor, sseSource) = Source
      .actorRef[String](
        completionMatcher = {
          case Done =>
            // complete stream immediately if we send it Done
            CompletionStrategy.immediately
        },
        // never fail the stream because of a message
        failureMatcher = PartialFunction.empty,
        bufferSize = 100,
        overflowStrategy = OverflowStrategy.dropHead
      )
      .preMaterialize()

    sseActor ! "hola"
    sseActor ! "xmentor"
    sseActor ! "meze ladri"

    // val sseSource = Source.queue[Notification](1000, OverflowStrategy.dropHead).preMaterialize()

    Ok.chunked(sseSource via EventSource.flow)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

  def registerNotification() = Action {
    logger.info("Registering new notification")
    Ok
  }

}
