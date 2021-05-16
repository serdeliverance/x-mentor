package repositories.graph

import akka.Done
import global.{ApplicationResult, ApplicationResultExtended}
import models.configurations.RedisGraphConfiguration
import models.errors.NotFoundError
import models.{CourseNode, Student, Topic}
import play.api.Logging
import repositories.RedisGraphRepository
import repositories.graph.StudentRepository.{
  createStudentQuery,
  studentByCourseQuery,
  studentByTopicQuery,
  studentQuery
}
import repositories.graph.decoder.{CourseTag, StudentTag}
import util.{ApplicationResultUtils, MapMarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StudentRepository @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ApplicationResultUtils {

  def getStudent(username: String)(implicit mmc: MapMarkerContext): ApplicationResult[Student] =
    redisGraphRepository
      .executeQuery[Student](studentQuery(username), StudentTag)
      .innerMap { result =>
        Right(result.headOption)
      }
      .innerMap {
        case Some(student) => Right(student)
        case None =>
          logger.info(s"Student: $username not found")
          Left(NotFoundError(s"Student: $username not found"))
      }

  def getStudentsByTopic(topic: Topic): ApplicationResult[List[Student]] =
    redisGraphRepository.executeQuery[Student](studentByTopicQuery(topic), StudentTag)

  def getStudentByCourse(course: CourseNode): ApplicationResult[List[Student]] =
    redisGraphRepository.executeQuery[Student](studentByCourseQuery(course.name), StudentTag)

  def createStudent(student: Student): ApplicationResult[Done] = {
    logger.info(s"Creating student: $student")
    redisGraphRepository.executeCreateQuery(createStudentQuery(student))
  }
}

object StudentRepository {

  private val studentQuery = (username: String) =>
    s"MATCH (student:Student) WHERE student.username = '$username' RETURN student"

  private val studentByTopicQuery = (topic: Topic) =>
    s"MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) WHERE topic.name = '${topic.name}' RETURN student"

  private val studentByCourseQuery = (course: String) =>
    s"MATCH (student)-[:studying]->(course) WHERE course.name = '$course' RETURN student"

  private val createStudentQuery = (student: Student) =>
    s"CREATE (:Student {username: '${student.username}', email: '${student.email}'})"
}
