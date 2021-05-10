package repositories

import akka.Done
import akka.Done.done
import com.redislabs.redistimeseries.RedisTimeSeries
import global.ApplicationResult
import models.errors.EmptyResponse
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class RedisTimeSeriesRepository @Inject()(redisTimeSeries: RedisTimeSeries)(implicit ec: ExecutionContext)
    extends Logging {

  def create(key: String, labels: Map[String, String] = Map.empty): ApplicationResult[Done] = {
    logger.info(s"Creating timeseries key: $key with labels: $labels")
    Try(redisTimeSeries.create(key, 0, labels.asJava))
      .fold({ error =>
        logger.error(s"Error creating key: $key. Error: $error")
        ApplicationResult.error(EmptyResponse)
      }, _ => ApplicationResult(done()))
  }

  // TODO
  def getAll[T](keys: List[String]): ApplicationResult[List[T]] = ???

  // TODO
  def get[T](key: String): ApplicationResult[T] = ???
}
