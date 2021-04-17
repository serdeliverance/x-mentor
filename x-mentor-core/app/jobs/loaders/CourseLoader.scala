package jobs.loaders

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Framing, JsonFraming, Sink}
import akka.util.ByteString
import models.Course
import play.api.Logging
import repositories.RedisGraphRepository
import java.nio.file.Paths

import com.redislabs.modules.rejson.JReJSON
import io.circe.parser.decode
import javax.inject.{Inject, Singleton}
import scala.concurrent._
import io.circe.generic.auto._
import io.circe.syntax._
import constans._

@Singleton
class CourseLoader @Inject()(
  redisGraphRepository: RedisGraphRepository,
  redisJSON: JReJSON
)(implicit system: ActorSystem)
  extends Logging {

  private val COURSE_CSV_PATH = "conf/data/courses.csv"
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
    FileIO
      .fromPath(Paths.get(COURSE_JSON_PATH))
      .via(JsonFraming.objectScanner(Int.MaxValue))
      .map(_.utf8String)
      .map(decode[Course](_))
      .map(course => redisJSON.set(s"$COURSE_KEY${course.map(_.id).getOrElse(0)}", course.map(_.asJson).getOrElse(null)))
      .to(Sink.ignore)
      .run()
  }
}
