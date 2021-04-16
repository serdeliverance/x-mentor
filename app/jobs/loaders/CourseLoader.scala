package jobs.loaders

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import models.Course
import play.api.Logging
import repositories.RedisGraphRepository

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CourseLoader @Inject()(redisGraphRepository: RedisGraphRepository)(implicit system: ActorSystem) extends Logging {

  private val COURSE_CSV_PATH = "conf/data/courses.csv"

  def loadCourses(): Future[IOResult] = {
    logger.info("Loading courses into the graph")
    FileIO
      .fromPath(Paths.get(COURSE_CSV_PATH))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(Course)
      .mapAsync(1)(redisGraphRepository.createCourse)
      .to(Sink.ignore)
      .run()
  }
}
