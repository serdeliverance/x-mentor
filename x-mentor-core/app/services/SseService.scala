package services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import play.api.Logging
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import services.SseService.{heartbeat, CourseCreatedSseEvent, SseEvent}

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import io.circe.syntax._
import models.Course

@Singleton
class SseService @Inject()()(implicit system: ActorSystem) extends Logging {

  private val (eventSourceQueue, sseSource) = Source
    .queue[SseEvent](10000, OverflowStrategy.backpressure)
    .map {
      case CourseCreatedSseEvent(course, createdAt) =>
        course.asJson
          .deepMerge(Map("createdAt" -> createdAt.toString).asJson)
          .deepMerge(Map("read" -> false).asJson)
          .noSpaces
    }
    .via(EventSource.flow)
    .keepAlive(5.second, () => heartbeat)
    .toMat(BroadcastHub.sink)(Keep.both)
    .run()

  def pushEvent(event: SseEvent): Future[QueueOfferResult] = {
    logger.info(s"Pushing event to SSE event queue $event")
    eventSourceQueue.offer(event)
  }

  def getSseSource: Source[Event, NotUsed] = sseSource
}

object SseService {
  sealed trait SseEvent
  case class CourseCreatedSseEvent(course: Course, createdAt: LocalDateTime) extends SseEvent
  val heartbeat: Event = Event("", None, None)
}
