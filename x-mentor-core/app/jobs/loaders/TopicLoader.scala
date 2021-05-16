package jobs.loaders

import akka.Done
import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import global.ApplicationResult
import models.Topic
import play.api.Logging
import repositories.graph.TopicRepository

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TopicLoader @Inject()(topicRepository: TopicRepository)(implicit system: ActorSystem, ec: ExecutionContext)
    extends Logging {

  private val TOPIC_CSV_PATH = "conf/data/topics.csv"

  def loadTopics(): ApplicationResult[Done] = {
    logger.info("Loading topics into the graph")
    FileIO
      .fromPath(Paths.get(TOPIC_CSV_PATH))
      .via(Framing.delimiter(ByteString(System.lineSeparator()), 256, allowTruncation = true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Topic(None, slices(0), slices(1))
      })
      .mapAsync(1)(topicRepository.createTopic)
      .to(Sink.ignore)
      .run()
      .map(_ => Right(done()))
  }
}
