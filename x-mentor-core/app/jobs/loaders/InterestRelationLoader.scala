package jobs.loaders

import akka.Done
import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import global.ApplicationResult
import models.{Interest, Topic}
import play.api.Logging
import repositories.RedisGraphRepository

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InterestRelationLoader @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val INTEREST_RELATION_CSV_PATH = "conf/data/interest.csv"

  def loadInterestRelations(): ApplicationResult[Done] = {
    logger.info("Loading interest relations into the graph")
    FileIO
      .fromPath(Paths.get(INTEREST_RELATION_CSV_PATH))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Interest(slices(0), slices(1))
      })
      .mapAsync(1)(redisGraphRepository.createInterestRelation)
      .runWith(Sink.ignore)
      .map(_ => Right(done()))
  }

}
