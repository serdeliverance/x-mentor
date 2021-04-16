package repositories

import akka.Done
import akka.Done.done
import com.redislabs.redisgraph.impl.api.RedisGraph
import global.ApplicationResult
import models.configurations.RedisGraphConfiguration
import models.{Course, Rating, Topic}
import play.api.Logging
import repositories.RedisGraphRepository.{
  courseRecommendationQuery,
  createCourseQuery,
  createTopicQuery,
  topicRecommendationQuery
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RedisGraphRepository @Inject()(
    redisGraph: RedisGraph,
    redisGraphConfiguration: RedisGraphConfiguration
  )(implicit ec: ExecutionContext)
    extends Logging {

  def createTopic(topic: Topic): ApplicationResult[Done] = {
    logger.info(s"Creating topic: $topic")
    executeQuery(redisGraphConfiguration.graph, createTopicQuery(topic))
  }

  def createCourse(course: Course): ApplicationResult[Done] = {
    logger.info(s"Creating course: $course")
    executeQuery(redisGraphConfiguration.graph, createCourseQuery(course))
  }

  // TODO retrieve information from redis in order to get a Student and Course instance to perform a more semantic graph relation
  def recommendCourse(studentId: Long, courseId: Long): ApplicationResult[Done] = {
    logger.info(
      s"Applying query: (:Student {studentId: '$studentId'})--[:recommeds]-->(:Course {courseId: '$courseId') to graph: ${redisGraphConfiguration.graph}")
    executeQuery(redisGraphConfiguration.graph, courseRecommendationQuery(studentId, courseId))
  }

  def recommendTopic(studentId: Long, topicId: Long): ApplicationResult[Done] = {
    logger.info(
      s"Applying query: (:Student {studentId: '$studentId'})--[:recommeds]-->(:Topic {topicId: '$topicId') to graph: ${redisGraphConfiguration.graph}")
    executeQuery(redisGraphConfiguration.graph, topicRecommendationQuery(studentId, topicId))
  }

  // TODO define correct signature and implement
  def createRecommendRelation(userId: Long, topicId: Option[Long], courseId: Option[Long]): ApplicationResult[Done] =
    ???

  // TODO define correct signature and implement
  def createRatesRelation(rating: Rating): ApplicationResult[Done] = ???

  private def executeQuery(graph: String, query: String): ApplicationResult[Done] =
    ApplicationResult {
      redisGraph.query(graph, query)
    }.map(_ => Right(done()))
}

object RedisGraphRepository {

  private val createTopicQuery = (topic: Topic) =>
    s"CREATE (:Topic {name: '${topic.name}', description: '${topic.description}'})"

  private val createCourseQuery = (course: Course) => s"CREATE (:Course {name: '${course.name}'})"

  private val courseRecommendationQuery = (studentId: Long, courseId: Long) =>
    s"CREATE (:Student {student_id: '$studentId'})-[:recommends]->(:Course {course_id: '$courseId'})"

  private val topicRecommendationQuery = (studentId: Long, topicId: Long) =>
    s"CREATE (:Student {student_id: '$studentId'})-[:recommends]->(:Topic {topic_id: '$topicId'})"
}
