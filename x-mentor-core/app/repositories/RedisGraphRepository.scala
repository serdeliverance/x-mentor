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

  def createRatesRelation(rating: Rating): ApplicationResult[Done] =
    executeCreateQuery(createRatesQuery(rating))

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

  private val coursesQuery = "MATCH (course:Course) RETURN course"

  private val topicsQuery = "MATCH (topic:Topic) RETURN topic"

  private val createRatesQuery = (rating: Rating) =>
    s"MATCH (s:Student), (c:Course) WHERE s.name = '${rating.student}' AND c.name = '${rating.course}' CREATE (s)-[:rates {rating:${rating.stars}}]->(c)"
}
