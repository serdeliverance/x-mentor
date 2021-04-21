package jobs.loaders

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import models.Student
import play.api.Logging
import repositories.RedisGraphRepository

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StudentLoader @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val STUDENT_CSV_PATH = "conf/data/students.csv"

  def loadStudents(): Future[IOResult] = {
    logger.info("Loading students into the graph")
    FileIO
      .fromPath(Paths.get(STUDENT_CSV_PATH))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Student(slices(0), slices(1))
      })
      .mapAsync(1)(redisGraphRepository.createStudent)
      .to(Sink.ignore)
      .run()
  }
}
