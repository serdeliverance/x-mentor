package jobs.loaders

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Framing, GraphDSL, RunnableGraph, Sink}
import akka.util.ByteString
import models.Student
import play.api.Logging
import repositories.RedisBloomRepository

import java.nio.file.Paths
import akka.Done.done
import akka.stream.ClosedShape
import constants._
import repositories.graph.StudentRepository

import javax.inject.{Inject, Singleton}
import services.UserService
import util.MapMarkerContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StudentLoader @Inject()(
    studentRepository: StudentRepository,
    redisBloomRepository: RedisBloomRepository,
    userService: UserService
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  private val STUDENT_CSV_PATH = "conf/data/students.csv"

  def loadStudents(): Future[Done] =
    Future(students().run()).map(_ => done())

  def loadStudentsToGraph(): Future[Done] = {
    logger.info("Loading students into the graph")
    FileIO
      .fromPath(Paths.get(STUDENT_CSV_PATH))
      .via(Framing.delimiter(ByteString(System.lineSeparator()), 256, allowTruncation = true).map(_.utf8String))
      .map(line => {
        val slices = line.split(",")
        Student(slices(0), slices(1))
      })
      .mapAsync(1)(studentRepository.createStudent)
      .runWith(Sink.ignore)
  }

  private def students(): RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      implicit val markerContext: MapMarkerContext = MapMarkerContext.apply()
      logger.info("Loading students into the graph")

      val source = FileIO
        .fromPath(Paths.get(STUDENT_CSV_PATH))
        .via(
          Framing
            .delimiter(ByteString(System.lineSeparator()), 256, allowTruncation = true)
            .map(_.utf8String))

      val convertToStudent = Flow[String]
        .map(line => {
          val slices = line.split(",")
          Student(slices(0), slices(1))
        })

      val createUsers = Flow[Student]
        .map(student => {
          userService.signup(student.username, student.username)
          student
        })

      val redisBloomSink = Flow[Student]
        .mapAsync(1)(student => redisBloomRepository.add(USERS_FILTER, student.username))
        .to(Sink.ignore)

      source ~> convertToStudent ~> createUsers ~> redisBloomSink
      ClosedShape
    })
}
