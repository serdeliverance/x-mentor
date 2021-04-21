package jobs.loaders

import akka.actor.ActorSystem
import global.ApplicationResult
import io.redisearch.Schema
import io.redisearch.client.Client
import javax.inject.{Inject, Singleton}
import play.api.Logging
import repositories.RediSearchRepository
import io.redisearch.client.IndexDefinition

import scala.concurrent._

@Singleton
class IndexLoader @Inject()(
    rediSearchRepository: RediSearchRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  def loadIndexes(): ApplicationResult[Unit] =
    ApplicationResult {
      logger.info("Creating indexes")
      val schema = new Schema()
                        .addSortableTextField("$.title", 1.0)
                        .addSortableTextField("$.description", 1.0)
      val definition = new IndexDefinition().setPrefixes(List("course:"):_*)
      rediSearchRepository.createIndex(schema, Client.IndexOptions.defaultOptions().setDefinition(definition))
    }

}
