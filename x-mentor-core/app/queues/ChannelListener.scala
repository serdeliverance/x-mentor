package queues

import akka.actor.ActorRef
import io.circe.Decoder
import io.circe.parser.decode
import play.api.Logging
import redis.clients.jedis.JedisPubSub

class ChannelListener[T](handler: ActorRef)(implicit decoder: Decoder[T]) extends JedisPubSub with Logging {

  // TODO fix logger
  override def onMessage(channel: String, message: String): Unit = {
    logger.info(s"Message received. Channel: $channel, Msg: $message")
    decode[T](message)
      .fold(
        error => {
          // TODO replace println with logger
          println(s"Error decoding message: $error")
        },
        command => {
          logger.info(s"Message decoded successfully: $command")
          handler ! command
        }
      )

  }

  override def onPMessage(pattern: String, channel: String, message: String): Unit = {}

  override def onSubscribe(channel: String, subscribedChannels: Int): Unit =
    // TODO replace with logging
    println(s"Subscribed to channel: $channel")

  override def onUnsubscribe(channel: String, subscribedChannels: Int): Unit =
    // TODO replace with logging
    println(s"Unsubscribed to channel: $channel")

  override def onPUnsubscribe(pattern: String, subscribedChannels: Int): Unit = {}

  override def onPSubscribe(pattern: String, subscribedChannels: Int): Unit = {}
}
