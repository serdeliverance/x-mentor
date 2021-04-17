package jobs.loaders

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import models.Topic
import play.api.Logging
import repositories.RedisGraphRepository

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class TopicLoader @Inject()(redisGraphRepository: RedisGraphRepository)(implicit system: ActorSystem) extends Logging {

  private val TOPIC_CSV_PATH = "conf/data/topics.csv"

  def loadTopics(): Future[IOResult] = {
    logger.info("Loading topics into the graph")
    FileIO
      .fromPath(Paths.get(TOPIC_CSV_PATH))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Topic(None, slices(0), slices(1))
      })
      .mapAsync(1)(redisGraphRepository.createTopic)
      .to(Sink.ignore)
      .run()
  }
}
