package jobs.loaders

import akka.Done
import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import global.ApplicationResult
import play.api.Logging
import services.MetricsService
import util.MapMarkerContext

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StudentProgressLoader @Inject()(
    metricsService: MetricsService
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val STUDENT_PROGRESS_CSV_PATH = "conf/data/studentProgress.csv"

  def loadStudentProgress(): ApplicationResult[Done] = {
    logger.info("Loading student progress into timeseries")
    implicit val mmc = MapMarkerContext()

    FileIO
      .fromPath(Paths.get(STUDENT_PROGRESS_CSV_PATH))
      .via(Framing.delimiter(ByteString(System.lineSeparator()), 256, allowTruncation = true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        (slices(0), slices(1))
      })
      .mapAsync(1)(studentProgress =>
        metricsService.registerStudentProgress(studentProgress._1, studentProgress._2.toInt))
      .to(Sink.ignore)
      .run()
      .map(_ => Right(done()))
  }
}
