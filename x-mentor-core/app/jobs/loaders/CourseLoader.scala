package jobs.loaders

import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, JsonFraming, RunnableGraph, Sink}
import akka.{Done, NotUsed}
import constants._
import io.circe.parser.decode
import io.rebloom.client.Client
import models.Course
import play.api.Logging
import repositories.{RedisBloomRepository, RedisGraphRepository, RedisJsonRepository, RedisRepository}
import util.CourseConverter

import java.nio.file.Paths
import global.EitherResult
import repositories.graph.CourseRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent._
import scala.jdk.CollectionConverters._

@Singleton
class CourseLoader @Inject()(
    courseRepository: CourseRepository,
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

  def loadCoursesToGraph(): Future[Int] =
    FileIO
      .fromPath(Paths.get(COURSE_JSON_PATH))
      .via(JsonFraming.objectScanner(Int.MaxValue))
      .map(_.utf8String)
      .map(decode[Course](_))
      .collect {
        case Right(course) => course
      }
      .filter(course => course.id.nonEmpty)
      .mapAsync(1)(courseRepository.createCourse)
      .runWith(Sink.seq[EitherResult[Done]])
      .map(list => list.size)

  private def graph(): RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      redisRepository.set(COURSE_LAST_ID_KEY, "40")

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
        .mapAsync(1)(course => redisBloomRepository.add(COURSE_IDS_FILTER, course.id.get.toString))
        .to(Sink.ignore)

      val redisJsonSink = Flow[Course]
        .map(course => (s"$COURSE_KEY${course.id.get}", course))
        .mapAsync(1)(courseIdAndCourse =>
          redisJsonRepository.set(courseIdAndCourse._1, CourseConverter.courseToMap(courseIdAndCourse._2).asJava))
        .to(Sink.ignore)

      val broadcast = builder.add(Broadcast[Course](2))

      source ~> convertToJson ~> broadcast ~> redisBloomSink
      broadcast ~> redisJsonSink
      ClosedShape
    })

}
