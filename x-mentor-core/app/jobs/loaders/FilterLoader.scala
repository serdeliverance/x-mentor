package jobs.loaders

import akka.Done
import akka.actor.ActorSystem
import cats.data.EitherT
import constants.{COURSE_IDS_FILTER, USERS_FILTER}
import play.api.Logging
import javax.inject.{Inject, Singleton}
import repositories.RedisBloomRepository
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FilterLoader @Inject()(redisBloomRepository: RedisBloomRepository)(implicit system: ActorSystem, ec: ExecutionContext) extends Logging {

  def loadFilters(): Future[Unit] =
    Future {
      logger.info("Creating bloom filters")

      for {
        _ <- EitherT(redisBloomRepository.deleteFilter(COURSE_IDS_FILTER))
        _ <- EitherT(redisBloomRepository.deleteFilter(USERS_FILTER))
        _ <- EitherT(redisBloomRepository.createFilter(COURSE_IDS_FILTER))
        _ <- EitherT(redisBloomRepository.createFilter(USERS_FILTER))
      } yield Done

    }
}
