package repositories.graph

import akka.Done
import global.{ApplicationResult, ApplicationResultExtended}
import models.{CourseNode, Student, Topic}
import models.configurations.RedisGraphConfiguration
import models.errors.NotFoundError
import play.api.Logging
import repositories.RedisGraphRepository
import repositories.graph.TopicRepository.{
  createTopicQuery,
  enrolledTopicsByStudentQuery,
  interestsByStudent,
  topicByCourseQuery,
  topicsQuery
}
import repositories.graph.decoder.{ResultDecoder, TopicTag}
import util.ApplicationResultUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TopicRepository @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ResultDecoder
    with ApplicationResultUtils {

  def getTopics(): ApplicationResult[List[Topic]] = {
    import models.Topic._
    redisGraphRepository.executeQuery[Topic](topicsQuery, TopicTag)
  }

  def getTopicByCourse(course: CourseNode): ApplicationResult[Topic] =
    redisGraphRepository
      .executeQuery[Topic](topicByCourseQuery(course.name), TopicTag)
      .innerMap(result => Right(result.headOption))
      .innerMap {
        case Some(topic) => Right(topic)
        case None =>
          logger.info(s"Not found topic for course: ${course.name}")
          Left(NotFoundError(s"not found topic for course: ${course.name}"))
      }

  def getInterestTopicsByStudent(student: String): ApplicationResult[List[Topic]] =
    redisGraphRepository.executeQuery[Topic](interestsByStudent(student), TopicTag)

  def getEnrolledTopics(student: Student): ApplicationResult[List[Topic]] =
    redisGraphRepository.executeQuery[Topic](enrolledTopicsByStudentQuery(student), TopicTag)

  def createTopic(topic: Topic): ApplicationResult[Done] = {
    logger.info(s"Creating topic: ${topic.name}")
    redisGraphRepository.executeCreateQuery(createTopicQuery(topic))
  }
}

object TopicRepository {
  private val topicsQuery = "MATCH (topic:Topic) RETURN topic"

  private val topicByCourseQuery = (course: String) =>
    s"MATCH (topic:Topic)-[:has]->(course:Course) WHERE course.name = '$course' RETURN topic"

  private val interestsByStudent = (student: String) =>
    s"MATCH (student)-[:interested]->(topic) WHERE student.username ='$student' RETURN topic"

  private val enrolledTopicsByStudentQuery = (student: Student) =>
    s"MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) WHERE student.username = '${student.username}' RETURN topic"

  private val createTopicQuery = (topic: Topic) =>
    s"CREATE (:Topic {name: '${topic.name}', description: '${topic.description}'})"
}
