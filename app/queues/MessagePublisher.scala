package queues

import akka.Done
import global.ApplicationResult
import io.circe.Encoder
import io.circe.syntax._
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool
import repositories.RedisExecution

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MessagePublisher @Inject()(val pool: Pool[Jedis])(implicit ec: ExecutionContext) extends RedisExecution {

  // TODO add logging to know publish action result
  def publish[T](channel: String, body: T)(implicit encoder: Encoder[T]): ApplicationResult[Done] =
    execute(false)(jedis => jedis.publish(channel, body.asJson.toString()) > 0)
      .map(_ => Right(Done))

}
