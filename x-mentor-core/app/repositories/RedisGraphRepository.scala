package repositories

import akka.Done
import akka.Done.done
import com.redislabs.redisgraph.impl.api.RedisGraph
import global.ApplicationResult
import models.configurations.RedisGraphConfiguration
import play.api.Logging
import repositories.graph.decoder.{GraphEntityTag, NodeDecoder, ResultDecoder}
import util.ApplicationResultUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RedisGraphRepository @Inject()(
    redisGraph: RedisGraph
  )(implicit
    ec: ExecutionContext)
    extends Logging
    with ResultDecoder
    with ApplicationResultUtils {

  def executeQuery[T](
      query: String,
      entityTag: GraphEntityTag
    )(implicit redisGraphConfiguration: RedisGraphConfiguration,
      nodeDecoder: NodeDecoder[T]
    ): ApplicationResult[List[T]] =
    ApplicationResult {
      val result = redisGraph.query(redisGraphConfiguration.graph, query)
      decode(result, entityTag)
    }

  def executeCreateQuery(
      query: String
    )(implicit redisGraphConfiguration: RedisGraphConfiguration
    ): ApplicationResult[Done] =
    ApplicationResult {
      logger.info(s"Running query: ${query.take(200)}")
      redisGraph.query(redisGraphConfiguration.graph, query)
    }.map(_ => Right(done()))
}
