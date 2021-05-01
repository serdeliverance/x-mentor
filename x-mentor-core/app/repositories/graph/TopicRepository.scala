package repositories.graph

import akka.Done
import global.ApplicationResult
import models.{Student, Topic}
import models.configurations.RedisGraphConfiguration
import play.api.Logging
import repositories.RedisGraphRepository
import repositories.graph.TopicRepository.{
  createTopicQuery,
  enrolledTopicsByStudentQuery,
  interestsByStudent,
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

  private val interestsByStudent = (student: String) =>
    s"MATCH (student)-[:interested]->(topic) where student.username ='$student' RETURN topic"

  private val enrolledTopicsByStudentQuery = (student: Student) =>
    s"MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) where student.username = '${student.username}' RETURN topic"

  private val createTopicQuery = (topic: Topic) =>
    s"CREATE (:Topic {name: '${topic.name}', description: '${topic.description}'})"
}
