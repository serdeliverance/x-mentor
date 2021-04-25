package jobs.loaders

import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, JsonFraming, RunnableGraph, Sink}
import akka.{Done, NotUsed}
import constants.{COURSE_KEY, COURSE_LAST_ID_KEY}
import io.circe.parser.decode
import io.rebloom.client.Client
import models.Course
import play.api.Logging
import repositories.{RedisBloomRepository, RedisGraphRepository, RedisJsonRepository, RedisRepository}
import util.CourseConverter

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent._
import scala.jdk.CollectionConverters._

@Singleton
class CourseLoader @Inject()(
    redisGraphRepository: RedisGraphRepository,
    redisJsonRepository: RedisJsonRepository,
    redisBloomRepository: RedisBloomRepository,
    redisRepository: RedisRepository,
    redisBloom: Client
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val COURSE_JSON_PATH = "conf/data/courses.json"

  def loadCourses(): Future[Done] =
    Future(graph().run()).map(_ => done())

  def loadCoursesToGraph(): Future[Done] =
    FileIO
      .fromPath(Paths.get(COURSE_JSON_PATH))
      .via(JsonFraming.objectScanner(Int.MaxValue))
      .map(_.utf8String)
      .map(decode[Course](_))
      .collect {
        case Right(course) => course
      }
      .filter(course => course.id.nonEmpty)
      .mapAsync(1)(redisGraphRepository.createCourse)
      .runWith(Sink.ignore)

  private def graph(): RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      redisRepository.set(COURSE_LAST_ID_KEY, "20")

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

//      val redisGraphSink = Flow[Course]
//        .mapAsync(1)(redisGraphRepository.createCourse)
//        .to(Sink.ignore)

      val redisJsonSink = Flow[Course]
        .map(course => (s"$COURSE_KEY${course.id.get}", course))
        .mapAsync(1)(courseIdAndCourse =>
          redisJsonRepository.set(courseIdAndCourse._1, CourseConverter.courseToMap(courseIdAndCourse._2).asJava))
        .to(Sink.ignore)

      val broadcast = builder.add(Broadcast[Course](2))

      source ~> convertToJson ~> broadcast ~> redisBloomSink
//      broadcast ~> redisGraphSink
      broadcast ~> redisJsonSink
      ClosedShape
    })

}
