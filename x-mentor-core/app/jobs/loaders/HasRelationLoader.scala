package jobs.loaders

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import models.Has
import play.api.Logging
import repositories.graph.RelationsRepository
import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HasRelationLoader @Inject()(
    relationsRepository: RelationsRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val HAS_RELATION_CSV_PATH = "conf/data/has.csv"

  def loadHasRelations(): Future[Done] = {
    logger.info("Loading has relation into the graph")

    FileIO
      .fromPath(Paths.get(HAS_RELATION_CSV_PATH))
      .via(Framing.delimiter(ByteString(System.lineSeparator()), 256, allowTruncation = true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Has(slices(0), slices(1))
      })
      .mapAsync(1)(relationsRepository.createHasRelation)
      .runWith(Sink.ignore)
  }
}
