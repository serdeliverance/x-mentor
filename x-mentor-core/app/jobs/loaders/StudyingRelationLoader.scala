package jobs.loaders

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import play.api.Logging
import repositories.RedisGraphRepository
import java.nio.file.Paths

import javax.inject.{Inject, Singleton}
import models.Studying

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StudyingRelationLoader @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val STUDYING_RELATION_CSV_PATH = "conf/data/studying.csv"

  def loadStudyingRelations(): Future[Done] = {
    logger.info("Loading studying relation into the graph")

    FileIO
      .fromPath(Paths.get(STUDYING_RELATION_CSV_PATH))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Studying(slices(0), slices(1))
      })
      .mapAsync(1)(redisGraphRepository.createStudyingRelation)
      .runWith(Sink.ignore)
  }
}
