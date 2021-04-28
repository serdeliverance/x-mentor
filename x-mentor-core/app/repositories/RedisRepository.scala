package repositories

import akka.Done
import akka.Done.done
import global.ApplicationResult
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams
import redis.clients.jedis.util.Pool
import javax.inject.{Inject, Singleton}
import models.errors.{ApplicationError, EmptyResponse}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

@Singleton
class RedisRepository @Inject()(val pool: Pool[Jedis])(implicit ec: ExecutionContext)
    extends RedisExecution
    with Logging {

  private val OK = "OK"

  def set(key: String, value: String, ttl: Option[FiniteDuration] = None): Future[Boolean] = {
    val params = new SetParams

    ttl.foreach { duration =>
      params.ex(duration.toSeconds.toInt)
    }

    execute(false)(jedis => Option(jedis.set(key, value, params)).contains(OK))
  }

  def get(key: String): Future[Option[String]] =
    execute[Option[String]](None)(jedis => Option(jedis.get(key)))

  def remove(key: String): Future[Boolean] = execute(false)(_.del(key) > 0)

  def hset(key: String, field: String, value: String): ApplicationResult[Done] = {
    logger.info(s"Creating hash for key: $key, field: $field, value: $value")
    Try(pool.getResource.hset(key, field, value))
      .fold(
        _ => {
          logger.info(s"Error adding hash to redis for key: $key.")
          ApplicationResult.error(EmptyResponse)
        },
        _ => ApplicationResult(done())
      )
  }
}
