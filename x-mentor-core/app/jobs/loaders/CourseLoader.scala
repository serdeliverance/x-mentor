package jobs.loaders

import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, JsonFraming, RunnableGraph, Sink}
import akka.{Done, NotUsed}
import constants.COURSE_KEY
import io.circe.parser.decode
import io.rebloom.client.Client
import models.Course
import play.api.Logging
import play.api.libs.json.Json._
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool
import repositories.{RedisBloomRepository, RedisGraphRepository, RedisJsonRepository}

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent._

@Singleton
class CourseLoader @Inject()(
    redisGraphRepository: RedisGraphRepository,
    redisJsonRepository: RedisJsonRepository,
    redisBloomRepository: RedisBloomRepository,
    redisPool: Pool[Jedis],
    redisBloom: Client
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val COURSE_JSON_PATH = "conf/data/courses.json"

  def loadJSONCourses(): Future[Done] =
    Future(graph().run()).map(_ => done())

  private def graph(): RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val source = FileIO
        .fromPath(Paths.get(COURSE_JSON_PATH))
        .via(JsonFraming.objectScanner(Int.MaxValue))
        .map(_.utf8String)

      val convertToJson = Flow[String]
        .map(decode[Course](_))
        .collect {
          case Right(course) => course
        }
        .filter(course => course.id.nonEmpty)

      val redisBloomSink = Flow[Course]
        .mapAsync(1)(course => redisBloomRepository.add(course))
        .to(Sink.ignore)

      val redisGraphSink = Flow[Course]
        .mapAsync(1)(redisGraphRepository.createCourse)
        .to(Sink.ignore)

      val redisJsonSink = Flow[Course]
        .map(course => (s"$COURSE_KEY${course.id.get}", course))
        .mapAsync(1)(courseIdAndCourse =>
          redisJsonRepository.set(courseIdAndCourse._1, s"'${toJson(courseIdAndCourse._2)}'"))
        .to(Sink.ignore)

      val broadcast = builder.add(Broadcast[Course](3))

      source ~> convertToJson ~> broadcast ~> redisBloomSink
      broadcast ~> redisGraphSink
      broadcast ~> redisJsonSink
      ClosedShape
    })

}
