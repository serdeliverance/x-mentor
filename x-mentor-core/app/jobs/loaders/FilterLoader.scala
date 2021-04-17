package jobs.loaders

import akka.actor.ActorSystem
import global.ApplicationResult
import io.rebloom.client.Client
import javax.inject.{Inject, Singleton}
import play.api.Logging
import constants.{COURSE_IDS_FILTER, USERS_FILTER}


@Singleton
class FilterLoader @Inject()(
    redisBloom: Client
  )(implicit system: ActorSystem) extends Logging {

  def loadFilters(): ApplicationResult[Unit] = {
    ApplicationResult {
      logger.info("Creating bloom filters")
      redisBloom.createFilter(COURSE_IDS_FILTER, 10000, 0.0001)
      redisBloom.createFilter(USERS_FILTER, 10000, 0.0001)
    }
  }
}
