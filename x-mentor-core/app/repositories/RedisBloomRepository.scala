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
          logger.error(s"Error adding value to bloom filter.")
          ApplicationResult.error(EmptyResponse)
        },
        _ => ApplicationResult(done())
      )

  def exists(filter: String, value: String): ApplicationResult[Boolean] =
    Try(redisBloom.exists(filter, value))
      .fold(
        _ => {
          logger.error(s"Error looking for value in bloom filter.")
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

  def createFilter(filter: String): ApplicationResult[Done] =
    Try(redisBloom.createFilter(filter, 10000, 0.0001))
      .fold(
        error => {
          logger.error(s"Error creating bloom filter $filter. Error: $error")
          ApplicationResult.error(EmptyResponse)
        },
        _ => {
          logger.info(s"'$filter' filter creation success")
          ApplicationResult(Done)
        }
      )

  def deleteFilter(filter: String): ApplicationResult[Done] =
    Try(redisBloom.delete(filter))
      .fold(
        error => {
          logger.error(s"Error deleting bloom filter $filter. Error: $error")
          ApplicationResult.error(EmptyResponse)
        },
        _ => {
          logger.info(s"'$filter' filter remove success")
          ApplicationResult(Done)
        }
      )
}
