package streams

import akka.Done
import global.ApplicationResult
import models.events.DomainEvent
import play.api.Logging
import redis.clients.jedis.util.Pool
import redis.clients.jedis.{Jedis, StreamEntryID}
import repositories.RedisExecution
import util.RedisStreamsUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

@Singleton
class MessagePublisher @Inject()(val pool: Pool[Jedis])(implicit ec: ExecutionContext)
    extends RedisExecution
    with Logging
    with RedisStreamsUtils {

  def publishEvent(stream: String, event: DomainEvent): ApplicationResult[Done] =
    execute(false)(jedis => {
      logger.info(s"Publishing event: $event into stream: $stream")
      jedis.xadd(stream, StreamEntryID.NEW_ENTRY, format(event).asJava)
      true // TODO refactor this return for something more meaningful
    }).map(_ => Right(Done))
}
