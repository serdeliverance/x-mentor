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

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class RedisTimeSeriesRepository @Inject()(redisTimeSeries: RedisTimeSeries)(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with UnixTimestampUtils
    with Logging {

  private val TIME_BUCKET_MILLIS = 2000
  private val RETENTION_TIME     = 0

  def create(key: String, labels: Map[String, String] = Map.empty): ApplicationResult[Done] = {
    logger.info(s"Creating timeseries key: $key with labels: $labels")
    Try(redisTimeSeries.create(key, RETENTION_TIME, labels.asJava))
      .fold(error => {
        logger.error(s"Error creating key: $key. Error: $error")
        ApplicationResult.error(EmptyResponse)
      }, _ => ApplicationResult(done()))
  }

  def getAll[T](keys: List[String])(implicit decoder: SampleDecoder[T]): ApplicationResult[Seq[T]] =
    sequence(keys.map(key => get[T](key)))

  def get[T](key: String)(implicit decoder: SampleDecoder[T]): ApplicationResult[T] =
    Try(
      redisTimeSeries
        .range(key, threeMonthsBack(), now(), Aggregation.SUM, TIME_BUCKET_MILLIS))
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
