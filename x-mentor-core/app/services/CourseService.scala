package services

import akka.Done
import akka.Done.done
import com.redislabs.modules.rejson.JReJSON
import constants.{COURSE_KEY, COURSE_LAST_ID_KEY}
import global.ApplicationResult
import io.rebloom.client.Client

import javax.inject.{Inject, Singleton}
import models.Course
import play.api.Logging
import play.api.libs.json.Json
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
      redisJsonRepository.set(s"$COURSE_KEY$currentIndex", s"'${toJson(updatedCourse)}'")
      redisInstance.incr(COURSE_LAST_ID_KEY)
    }.map(_ => Right(done()))

  def enroll(courseId: Long): ApplicationResult[Done] = ???

  def retrieveAll(): ApplicationResult[Done] = ???

//  def retrieveById(courseId: Long): ApplicationResult[Done] =
//    ApplicationResult {
//      val exists = redisBloom.exists("courses", courseId.toString)
//      if (exists) {
//        redisJsonRepository.get[String](s"$COURSE_KEY$courseId")
//      }
//    }.map(_ => Right(done()))

  def retrieveById(courseId: Long): ApplicationResult[Course] =
    redisJsonRepository.get[Course](s"$COURSE_KEY$courseId")

}
