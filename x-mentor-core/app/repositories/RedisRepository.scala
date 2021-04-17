package repositories

import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams
import redis.clients.jedis.util.Pool

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

@Singleton
class RedisRepository @Inject()(val pool: Pool[Jedis]) extends RedisExecution {

  private val OK = "OK"

  def set(key: String, value: String, ttl: Option[FiniteDuration] = None): Future[Boolean] = {
    val params = new SetParams

    ttl.foreach { duration =>
      params.ex(duration.toSeconds.toInt)
    }

    execute(false)(jedis => Option(jedis.set(key, value, params)).contains(OK))
  }

  def get(key: String): Future[Option[String]] = execute[Option[String]](None)(jedis => Option(jedis.get(key)))

  def remove(key: String): Future[Boolean] = execute(false)(_.del(key) > 0)
}
