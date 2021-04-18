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
import repositories.{RedisGraphRepository, RedisJsonRepository, RedisRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CourseService @Inject()(
   redisBloom: Client,
   redisJsonRepository: RedisJsonRepository,
   redisRepository: RedisRepository,
   redisGraphRepository: RedisGraphRepository,
   redisPool: Pool[Jedis],
   rediSearch: io.redisearch.client.Client
  )(implicit ec: ExecutionContext)
    extends Logging {

  def create(course: Course): ApplicationResult[Done] =
    ApplicationResult {
      val redisInstance       = redisPool.getResource
      val currentIndex: Long  = redisInstance.get(COURSE_LAST_ID_KEY).toLong + 1
      val key                 = s"$COURSE_KEY$currentIndex"
      val updatedCourse       = course.copy(id = Some(currentIndex))

      logger.info(s"Storing course $currentIndex in Redis, increasing last id and adding to bloom filter")
      // Insert into redisJSON
      redisJsonRepository.set(key, s"'${toJson(updatedCourse)}'")
      redisInstance.incr(COURSE_LAST_ID_KEY)
      // Insert into bloom filter
      redisBloom.add(COURSE_IDS_FILTER, currentIndex.toString)
    }.map(_ => Right(done()))

  def enroll(courseId: Long): ApplicationResult[Done] = ???

  def retrieveAll(): ApplicationResult[Done] =
    ApplicationResult {
      logger.info(s"Retrieving courses")
      val courses = redisGraphRepository.getCourses()
      val keys = courses.map {
        case courseList => courseList.map(courseList => {
          courseList.map(course => {
            logger.info(s"$course")
            s"$COURSE_KEY${course.id}"
          })
        })
        case _ => ???
      }
      keys.onComplete {
        case Success(data) => {
          logger.info(s"${data.getOrElse(List.empty)}")
          val courses = redisJsonRepository.getAll[String](data.getOrElse(List.empty))
          logger.info(s"$courses")
        }
        case Failure(ex) => println("ERROR occured - "+ex.getMessage)
      }
      Done
  }

  def retrieveById(courseId: Long): ApplicationResult[Course] =
    if (redisBloom.exists(COURSE_IDS_FILTER, courseId.toString)) {
      redisJsonRepository.get[Course](s"$COURSE_KEY$courseId")
    } else {
      ApplicationResult.error(NotFoundError("Course not found"))
    }
}
