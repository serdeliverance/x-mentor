package models.configurations

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.scaladsl.Source
import play.api.libs.EventSource.Event

case class SSEConfiguration(notificationActor: ActorRef, sseSource: Source[Event, NotUsed])
