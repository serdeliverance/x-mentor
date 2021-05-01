package repositories.graph

import akka.Done
import global.ApplicationResult
import models.configurations.RedisGraphConfiguration
import models.{Student, Topic}
import play.api.Logging
import repositories.RedisGraphRepository
import repositories.graph.StudentRepository.{createStudentQuery, studentByTopic}
import repositories.graph.decoder.StudentTag
import util.ApplicationResultUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StudentRepository @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ApplicationResultUtils {

  // TODO
  def getStudent(username: String): ApplicationResult[Student] = ???

  def getStudentsByTopic(topic: Topic): ApplicationResult[List[Student]] =
    redisGraphRepository.executeQuery[Student](studentByTopic(topic), StudentTag)

  def createStudent(student: Student): ApplicationResult[Done] = {
    logger.info(s"Creating student: $student")
    redisGraphRepository.executeCreateQuery(createStudentQuery(student))
  }
}

object StudentRepository {
  private val studentByTopic = (topic: Topic) =>
    s"MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) where topic.name = '${topic.name}' RETURN student"

  private val createStudentQuery = (student: Student) =>
    s"CREATE (:Student {username: '${student.username}', email: '${student.email}'})"
}
