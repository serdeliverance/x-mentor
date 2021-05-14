package repositories

import akka.Done
import akka.Done.done
import com.redislabs.redistimeseries.{Aggregation, RedisTimeSeries, Value}
import global.ApplicationResult
import models.errors.EmptyResponse
import play.api.Logging
import repositories.timeseries.SampleDecoder
import repositories.timeseries.SampleDecoderSyntax.decodeSample
import util.{ApplicationResultUtils, UnixTimestampUtils}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class RedisTimeSeriesRepository @Inject()(redisTimeSeries: RedisTimeSeries)(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with UnixTimestampUtils
    with Logging {

  private val TIME_BUCKET_MILLIS = 1000
  private val RETENTION_TIME     = 0

  def create(key: String, labels: Map[String, String] = Map.empty): ApplicationResult[Done] = {
    logger.info(s"Creating timeseries key: $key with labels: $labels")
    Try(redisTimeSeries.create(key, RETENTION_TIME, labels.asJava))
      .fold(error => {
        logger.error(s"Error creating key: $key. Error: $error")
        ApplicationResult.error(EmptyResponse)
      }, _ => ApplicationResult(done()))
  }

  /**
    * For all the specified keys, get the samples summarization in a time window of three months.
    */
  def forAllThreeMonthsRangeSummarized[T](
      keys: List[String]
    )(implicit decoder: SampleDecoder[T]
    ): ApplicationResult[Seq[T]] =
    sequence(keys.map(key => getLastThreeMonthsRageSummarized[T](key)))

  /**
    * For the specified key, summarizes sample values on a time window of three months
    */
  def getLastThreeMonthsRageSummarized[T](key: String)(implicit decoder: SampleDecoder[T]): ApplicationResult[T] =
    getRangeSummarized[T](key: String, threeMonthsBack(), now())

  /**
    * For the specified key, get the samples by range performing the sum aggregation over them
    */
  def getRangeSummarized[T](
      key: String,
      from: Long,
      to: Long
    )(implicit decoder: SampleDecoder[T]
    ): ApplicationResult[T] =
    Try(
      redisTimeSeries
        .range(key, from, to, Aggregation.SUM, TIME_BUCKET_MILLIS))
      .fold(
        error => {
          logger.error(s"Error retrieving key: $key from timeseries. Error: $error")
          ApplicationResult.error(EmptyResponse)
        },
        sample => decodeTimeSeriesSample[T](sample, key)
      )

  private def decodeTimeSeriesSample[T](
      sample: Array[Value],
      key: String
    )(implicit sampleDecoder: SampleDecoder[T]
    ): ApplicationResult[T] =
    if (sample.nonEmpty) {
      decodeSample[T](key, sample).fold(error => {
        logger.error(s"Error decoding sample. Error: $error")
        ApplicationResult.error(EmptyResponse)
      }, decodedValue => ApplicationResult(decodedValue))
    } else {
      logger.debug(s"No samples loaded yet for key $key")
      ApplicationResult.error(EmptyResponse)
    }
}
