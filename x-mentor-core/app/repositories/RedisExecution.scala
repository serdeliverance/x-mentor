package repositories

import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool

import scala.concurrent.Future

trait RedisExecution {

  def pool(): Pool[Jedis]

  def execute[T](default: => T)(action: Jedis => T): Future[T] = Future.successful {
    var maybeJedis: Option[Jedis] = None

    try {
      maybeJedis = Some(pool.getResource)
      action(maybeJedis.get)
    } catch {
      case _: Throwable => default
    } finally {
      maybeJedis.foreach { jedis =>
        jedis.close()
      }
    }
  }
}
