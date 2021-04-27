package repositories

import akka.Done
import akka.Done.done
import global.ApplicationResult
import io.rebloom.client.Client
import models.errors.EmptyResponse
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class RedisBloomRepository @Inject()(redisBloom: Client) extends Logging {

  def add(filter: String, value: String): ApplicationResult[Done] =
    Try(redisBloom.add(filter, value))
      .fold(
        _ => {
          logger.info(s"Error adding value to redis blooms.")
          ApplicationResult.error(EmptyResponse)
        },
        _ => ApplicationResult(done())
      )

}
