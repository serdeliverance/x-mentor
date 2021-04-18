package services

import akka.Done
import akka.Done.done
import constants.{COURSE_IDS_FILTER, COURSE_KEY, COURSE_LAST_ID_KEY}
import global.ApplicationResult
import io.rebloom.client.Client
import javax.inject.{Inject, Singleton}
import models.Course
import models.errors.NotFoundError
import play.api.Logging
import play.api.libs.json.Json.toJson
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool
import repositories.RedisJsonRepository

import scala.concurrent.ExecutionContext

@Singleton
class CourseService @Inject()(
    redisBloom: Client,
    redisJsonRepository: RedisJsonRepository,
    redisPool: Pool[Jedis]
  )(implicit ec: ExecutionContext)
    extends Logging {

  def create(course: Course): ApplicationResult[Done] =
    ApplicationResult {
      val redisInstance      = redisPool.getResource
      val currentIndex: Long = redisInstance.get(COURSE_LAST_ID_KEY).toLong + 1
      logger.info(s"Current Index: $currentIndex")
      val updatedCourse = course.copy(id = Some(currentIndex))
      logger.info(s"Storing course in Redis, increasing last id and adding to bloom filter")
      redisJsonRepository.set(s"$COURSE_KEY$currentIndex", s"'${toJson(updatedCourse)}'")
      redisInstance.incr(COURSE_LAST_ID_KEY)
      redisBloom.add(COURSE_IDS_FILTER, currentIndex.toString)
    }.map(_ => Right(done()))

  def enroll(courseId: Long): ApplicationResult[Done] = ???

  def retrieveAll(): ApplicationResult[List[Course]] = ???
      //redisJsonRepository.getAll[Course](s"$COURSE_KEY")

  def retrieveById(courseId: Long): ApplicationResult[Course] =
    if (redisBloom.exists(COURSE_IDS_FILTER, courseId.toString)) {
      redisJsonRepository.get[Course](s"$COURSE_KEY$courseId")
    } else {
      ApplicationResult.error(NotFoundError("Course not found"))
    }
}
