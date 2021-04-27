package repositories

import akka.Done
import akka.Done.done
import com.redislabs.redisgraph.impl.api.RedisGraph
import constants.ITEMS_PER_PAGE
import global.{ApplicationResult, ApplicationResultExtended}
import jobs.loaders.Studying
import models.configurations.RedisGraphConfiguration
import models._
import play.api.Logging
import repositories.RedisGraphRepository._
import repositories.graph._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RedisGraphRepository @Inject()(
    redisGraph: RedisGraph,
)(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ResultDecoder {

  def getCourses(): ApplicationResult[List[CourseNode]] =
    executeQuery[CourseNode](coursesQuery, CourseTag)

  def getTopics(): ApplicationResult[List[Topic]] = {
    import models.Topic._
    executeQuery[Topic](topicsQuery, TopicTag)
  }

  def getCoursesByStudentPaginated(student: String, page: Int): ApplicationResult[List[CourseNode]] = {
    val offset = (page - 1) * ITEMS_PER_PAGE
    executeQuery[CourseNode](paginated(coursesByStudentQuery(student), offset, ITEMS_PER_PAGE), CourseTag)
  }

  def getCoursesByStudent(student: String): ApplicationResult[List[CourseNode]] =
    executeQuery[CourseNode](coursesByStudentQuery(student), CourseTag)

  def getCoursesRatedByStudent(student: String): ApplicationResult[List[CourseNode]] =
    executeQuery[CourseNode](coursesRatedByStudent(student), CourseTag)

  def existsRatesRelation(student: String, course: String): ApplicationResult[Boolean] =
    getCoursesRatedByStudent(student).innerMap(result => Right(result.exists(_.name == course)))

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

  def createStudent(student: Student): ApplicationResult[Done] = {
    logger.info(s"Creating student: $student")
    executeCreateQuery(createStudentQuery(student))
  }

  def createRatesRelation(rating: Rating): ApplicationResult[Done] =
    executeCreateQuery(createRatesQuery(rating))

  def createHasRelation(hasRelation: Has): ApplicationResult[Done] =
    executeCreateQuery(createHasRelationQuery(hasRelation))

  def createInterestRelation(interest: Interest): ApplicationResult[Done] =
    executeCreateQuery(createInterestRelationQuery(interest))

  def createStudyingRelation(studying: Studying): ApplicationResult[Done] =
    executeCreateQuery(createStudyingRelationQuery(studying))

  def existsStudyingRelation(student: String, course: String): ApplicationResult[Boolean] =
    getCoursesByStudent(student).innerMap(result => Right(result.exists(_.name == course)))

  private def executeCreateQuery(
      query: String
    )(implicit redisGraphConfiguration: RedisGraphConfiguration
    ): ApplicationResult[Done] =
    ApplicationResult {
      logger.info(s"Running query: $query")
      redisGraph.query(redisGraphConfiguration.graph, query)
    }.map(_ => Right(done()))
}

object RedisGraphRepository {

  private val coursesQuery = "MATCH (course:Course) RETURN course"

  private val topicsQuery = "MATCH (topic:Topic) RETURN topic"

  private val paginated = (query: String, offset: Int, limit: Int) => s"$query SKIP $offset LIMIT $limit"

  private val coursesByStudentQuery = (student: String) =>
    s"MATCH (student)-[:studying]->(course) where student.username = '$student' RETURN course"

  private val coursesRatedByStudent = (student: String) =>
    s"MATCH (student)-[:rates]->(course) where student.username ='$student' RETURN course"

  private val createTopicQuery = (topic: Topic) =>
    s"CREATE (:Topic {name: '${topic.name}', description: '${topic.description}'})"

  private val createCourseQuery = (course: Course) =>
    s"CREATE (:Course {name: '${course.title}', id: '${course.id.get}'})"

  private val createStudentQuery = (student: Student) =>
    s"CREATE (:Student {username: '${student.username}', email: '${student.email}'})"

  private val createInterestRelationQuery = (interest: Interest) =>
    s"MATCH (s:Student), (t:Topic) WHERE s.username = '${interest.student}' AND t.name = '${interest.topic}' CREATE (s)-[:interested]->(t)"

  private val createHasRelationQuery = (hasRelation: Has) =>
    s"MATCH (t:Topic), (c:Course) WHERE t.name = '${hasRelation.topic}' AND c.name = '${hasRelation.course}' CREATE (t)-[:has]->(c)"

  private val createRatesQuery = (rating: Rating) =>
    s"MATCH (s:Student), (c:Course) WHERE s.username = '${rating.student}' AND c.name = '${rating.course}' CREATE (s)-[:rates {rating:${rating.stars}}]->(c)"

  private val createStudyingRelationQuery = (studying: Studying) =>
    s"MATCH (s:Student), (c:Course) WHERE s.username = '${studying.student}' AND c.name = '${studying.course}' CREATE (s)-[:studying]->(c)"
}
