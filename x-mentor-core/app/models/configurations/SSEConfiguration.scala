package models.configurations

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.Source

case class SSEConfiguration(notificationActor: ActorRef, sseSource: Source[String, NotUsed])
