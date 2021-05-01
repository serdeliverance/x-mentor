package repositories

import akka.Done
import akka.Done.done
import global.ApplicationResult
import io.rebloom.client.Client
import models.errors.{EmptyResponse, NotFoundError}
import play.api.Logging
import javax.inject.{Inject, Singleton}

import scala.util.Try

@Singleton
class RedisBloomRepository @Inject()(redisBloom: Client) extends Logging {

  def add(filter: String, value: String): ApplicationResult[Done] =
    Try(redisBloom.add(filter, value))
      .fold(
        _ => {
          logger.error(s"Error adding value to redis blooms.")
          ApplicationResult.error(EmptyResponse)
        },
        _ => ApplicationResult(done())
      )

  def exists(filter: String, value: String): ApplicationResult[Boolean] =
    Try(redisBloom.exists(filter, value))
      .fold(
        _ => {
          logger.error(s"Error adding value to redis blooms.")
          ApplicationResult.error(EmptyResponse)
        },
        response =>
          if (response) {
            logger.info("Object exists")
            ApplicationResult(true)
          } else {
            logger.info("Object does not exists")
            ApplicationResult(false)
          }
      )

}
