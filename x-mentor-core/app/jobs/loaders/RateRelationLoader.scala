package jobs.loaders

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import models.Rating
import play.api.Logging
import repositories.graph.RelationsRepository

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RateRelationLoader @Inject()(
    relationsRepository: RelationsRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val RATE_RELATION_CSV_PATH = "conf/data/rate.csv"

  def loadRateRelations(): Future[Done] = {
    logger.info("Loading rates relation into the graph")

    FileIO
      .fromPath(Paths.get(RATE_RELATION_CSV_PATH))
      .via(Framing.delimiter(ByteString(System.lineSeparator()), 256, true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Rating(slices(0), slices(2), slices(1).toInt)
      })
      .mapAsync(1)(relationsRepository.createRatesRelation)
      .runWith(Sink.ignore)
  }
}
