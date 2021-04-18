package repositories

import akka.Done
import akka.Done.done
import constants.COURSE_IDS_FILTER
import global.ApplicationResult
import io.rebloom.client.Client
import models.Course
import models.errors.EmptyResponse
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class RedisBloomRepository @Inject()(redisBloom: Client) extends Logging {

  def add(course: Course): ApplicationResult[Done] =
    Try(redisBloom.add(COURSE_IDS_FILTER, course.id.get.toString))
      .fold(
        _ => {
          logger.info(s"Error adding value to redis blooms.")
          ApplicationResult.error(EmptyResponse)
        },
        _ => ApplicationResult(done())
      )

}
