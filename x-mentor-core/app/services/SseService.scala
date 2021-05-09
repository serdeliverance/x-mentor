package services

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.Source
import play.api.Logging
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import services.SseService.{heartbeat, CourseCreatedSseEvent, SseEvent}

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@Singleton
class SseService @Inject()()(implicit system: ActorSystem) extends Logging {

  private val (eventSourceQueue, sseSource) = Source
    .queue[SseEvent](100, OverflowStrategy.backpressure)
    .map {
      case CourseCreatedSseEvent(course, createdAt) => s"$course $createdAt"
    }
    .via(EventSource.flow)
    .keepAlive(1.second, () => heartbeat)
    .preMaterialize()

  def pushEvent(event: SseEvent): Future[QueueOfferResult] = {
    logger.info(s"Pushing event to SSE event queue $event")
    eventSourceQueue.offer(event)
  }

  def getSseSource(): Source[Event, NotUsed] = sseSource
}

object SseService {
  sealed trait SseEvent
  case class CourseCreatedSseEvent(course: String, createdAt: LocalDateTime) extends SseEvent

  val heartbeat = Event("", None, None)
}
