package jobs.loaders

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, JsonFraming, Sink}
import constants.{COURSE_KEY, COURSE_LAST_ID_KEY}
import io.circe.generic.auto._
import io.circe.parser.decode
import models.Course
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.json.Json._
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool
import repositories.{RedisGraphRepository, RedisJsonRepository}

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent._

@Singleton
class CourseLoader @Inject()(
    redisGraphRepository: RedisGraphRepository,
    redisJsonRepository: RedisJsonRepository,
    redisPool: Pool[Jedis]
  )(implicit system: ActorSystem)
    extends Logging {

  implicit val writes          = Json.writes[Course]
  private val COURSE_CSV_PATH  = "conf/data/courses.csv"
  private val COURSE_JSON_PATH = "conf/data/courses.json"

  /*def loadCourses(): Future[IOResult] = {
    logger.info("Loading courses into the graph")
    FileIO
      .fromPath(Paths.get(COURSE_CSV_PATH))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(decode[Course](_))
      .mapAsync(1)(redisGraphRepository.createCourse)
      .to(Sink.ignore)
      .run()
  }*/

  def loadJSONCourses(): Future[IOResult] = {
    logger.info("Loading JSON courses into the redisJSON")
    redisPool.getResource.set(COURSE_LAST_ID_KEY, "19")
    FileIO
      .fromPath(Paths.get(COURSE_JSON_PATH))
      .via(JsonFraming.objectScanner(Int.MaxValue))
      .map(_.utf8String)
      .map(decode[Course](_))
      .collect {
        case Right(course) => course
      }
      .filter(course => course.id.nonEmpty)
      .map(course => (s"$COURSE_KEY${course.id.get}", course))
      .mapAsync(1)(courseIdAndCourse =>
        redisJsonRepository.set(courseIdAndCourse._1, s"'${toJson(courseIdAndCourse._2)}'"))
      .to(Sink.ignore)
      .run()
  }
}
