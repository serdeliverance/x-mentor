package jobs.loaders

import akka.actor.ActorSystem
import constants.{COURSE_IDS_FILTER, USERS_FILTER}
import io.rebloom.client.Client
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FilterLoader @Inject()(redisBloom: Client)(implicit system: ActorSystem, ec: ExecutionContext) extends Logging {

  def loadFilters(): Future[Unit] =
    Future {
      logger.info("Deleting bloom filters")
      val result = redisBloom.delete(COURSE_IDS_FILTER)
      logger.info(s"$COURSE_IDS_FILTER filter removed: $result")
      val result2 = redisBloom.delete(USERS_FILTER)
      logger.info(s"$USERS_FILTER filter removed: $result2")

      logger.info("Creating bloom filters")
      redisBloom.createFilter(COURSE_IDS_FILTER, 10000, 0.0001)
      redisBloom.createFilter(USERS_FILTER, 10000, 0.0001)
    }
}
