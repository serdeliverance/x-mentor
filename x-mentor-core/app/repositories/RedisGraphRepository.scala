package repositories

import akka.Done
import akka.Done.done
import com.redislabs.redisgraph.impl.api.RedisGraph
import global.ApplicationResult
import models.configurations.RedisGraphConfiguration
import models.{Course, CourseNode, Rating, Topic}
import play.api.Logging
import repositories.RedisGraphRepository._
import repositories.graph.{CourseTag, GraphEntityTag, NodeDecoder, ResultDecoder, TopicTag}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RedisGraphRepository @Inject()(
    redisGraph: RedisGraph,
)(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ResultDecoder {

  def getCourses(): ApplicationResult[List[CourseNode]] = {
    import models.Course._
    executeQuery[CourseNode](coursesQuery, CourseTag)
  }

  def getTopics(): ApplicationResult[List[Topic]] = {
    import models.Topic._
    executeQuery[Topic](topicsQuery, TopicTag)
  }

  def executeQuery[T](
      query: String,
      entityTag: GraphEntityTag
    )(implicit redisGraphConfiguration: RedisGraphConfiguration,
      nodeDecoder: NodeDecoder[T]
    ): ApplicationResult[List[T]] =
    ApplicationResult {
      val result = redisGraph.query(redisGraphConfiguration.graph, query)
      decode(result, entityTag)
    }

  def createTopic(topic: Topic): ApplicationResult[Done] = {
    logger.info(s"Creating topic: ${topic.name}")
    executeCreateQuery(createTopicQuery(topic))
  }

  def createCourse(course: Course): ApplicationResult[Done] = {
    logger.info(s"Creating course: $course")
    executeCreateQuery(createCourseQuery(course))
  }

  // TODO retrieve information from redis in order to get a Student and Course instance to perform a more semantic graph relation
  def recommendCourse(studentId: Long, courseId: Long): ApplicationResult[Done] = {
    logger.info(
      s"Applying query: (:Student {studentId: '$studentId'})--[:recommeds]-->(:Course {courseId: '$courseId') to graph: ${redisGraphConfiguration.graph}")
    executeCreateQuery(courseRecommendationQuery(studentId, courseId))
  }

  def recommendTopic(studentId: Long, topicId: Long): ApplicationResult[Done] = {
    logger.info(
      s"Applying query: (:Student {studentId: '$studentId'})--[:recommeds]-->(:Topic {topicId: '$topicId') to graph: ${redisGraphConfiguration.graph}")
    executeCreateQuery(topicRecommendationQuery(studentId, topicId))
  }

  // TODO define correct signature and implement
  def createRecommendRelation(userId: Long, topicId: Option[Long], courseId: Option[Long]): ApplicationResult[Done] =
    ???

  // TODO define correct signature and implement
  def createRatesRelation(rating: Rating): ApplicationResult[Done] = ???

  private def executeCreateQuery(
      query: String
    )(implicit redisGraphConfiguration: RedisGraphConfiguration
    ): ApplicationResult[Done] =
    ApplicationResult {
      redisGraph.query(redisGraphConfiguration.graph, query)
    }.map(_ => Right(done()))
}

object RedisGraphRepository {

  private val createTopicQuery = (topic: Topic) =>
    s"CREATE (:Topic {name: '${topic.name}', description: '${topic.description}'})"

  private val createCourseQuery = (course: Course) => s"CREATE (:Course {name: '${course.title}'})"

  private val courseRecommendationQuery = (studentId: Long, courseId: Long) =>
    s"CREATE (:Student {student_id: '$studentId'})-[:recommends]->(:Course {course_id: '$courseId'})"

  private val topicRecommendationQuery = (studentId: Long, topicId: Long) =>
    s"CREATE (:Student {student_id: '$studentId'})-[:recommends]->(:Topic {topic_id: '$topicId'})"

  private val coursesQuery = "MATCH (course:Course) RETURN course"

  private val topicsQuery = "MATCH (topic:Topic) RETURN topic"
}
