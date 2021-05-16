package jobs.loaders

import akka.actor.ActorSystem
import io.redisearch.client.{Client, ConfigOption}
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RedisearchLoader @Inject()(rediSearch: Client)(implicit system: ActorSystem, ec: ExecutionContext) extends Logging {

  def loadConfigs(): Future[Boolean] =
    Future {
      logger.info(s"Setting config MINPREFIX with value: 1")
      rediSearch.setConfig(ConfigOption.MINPREFIX, "1")
    }
}
