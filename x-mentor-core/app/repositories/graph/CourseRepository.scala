package repositories.graph

import akka.Done
import constants.ITEMS_PER_PAGE
import global.{ApplicationResult, ApplicationResultExtended}
import models.configurations.RedisGraphConfiguration
import models.{Course, CourseNode, Student, Topic}
import play.api.Logging
import repositories.RedisGraphRepository
import repositories.graph.CourseRepository._
import repositories.graph.decoder.{CourseTag, ResultDecoder}
import util.ApplicationResultUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CourseRepository @Inject()(
    redisGraphRepository: RedisGraphRepository
  )(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ResultDecoder
    with ApplicationResultUtils {

  def getCourses(): ApplicationResult[List[CourseNode]] =
    redisGraphRepository.executeQuery[CourseNode](coursesQuery, CourseTag)

  def getCoursesByStudent(student: String): ApplicationResult[List[CourseNode]] =
    redisGraphRepository.executeQuery[CourseNode](coursesByStudentQuery(student), CourseTag)

  def getCoursesByTopic(topic: Topic): ApplicationResult[List[CourseNode]] =
    redisGraphRepository.executeQuery[CourseNode](coursesByTopicQuery(topic), CourseTag)

  def getCoursesByStudentPaginated(student: String, page: Int): ApplicationResult[List[CourseNode]] = {
    val offset = (page - 1) * ITEMS_PER_PAGE
    redisGraphRepository
      .executeQuery[CourseNode](paginated(coursesByStudentQuery(student), offset, ITEMS_PER_PAGE), CourseTag)
  }

  def getCoursesRatedByStudent(student: String): ApplicationResult[List[CourseNode]] =
    redisGraphRepository.executeQuery[CourseNode](coursesRatedByStudent(student), CourseTag)

  def getCoursesByStudentAndTopic(student: Student, topic: Topic): ApplicationResult[List[CourseNode]] =
    redisGraphRepository.executeQuery[CourseNode](coursesByStudentAndTopic(student, topic), CourseTag)

  def getCoursesByStudentAndTopicInBulk(students: List[Student], topic: Topic): ApplicationResult[Seq[CourseNode]] =
    sequence {
      students.map(student => getCoursesByStudentAndTopic(student, topic))
    }.innerMap(result => Right(result.flatten.distinct))

  def createCourse(course: Course): ApplicationResult[Done] = {
    logger.info(s"Creating course: $course")
    redisGraphRepository.executeCreateQuery(createCourseQuery(course))
  }
}

object CourseRepository {

  private val coursesQuery = "MATCH (course:Course) RETURN course"

  private val coursesByStudentQuery = (student: String) =>
    s"MATCH (student)-[:studying]->(course) where student.username = '$student' RETURN course"

  private val coursesByTopicQuery = (topic: Topic) =>
    s"MATCH (topic)-[:has]->(course) WHERE topic.name = '${topic.name}' RETURN course"

  private val coursesRatedByStudent = (student: String) =>
    s"MATCH (student)-[:rates]->(course) where student.username ='$student' RETURN course"

  private val coursesByStudentAndTopic = (student: Student, topic: Topic) =>
    s"MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) where student.username = '${student.username}' and topic.name = '${topic.name}' RETURN course"

  private val paginated = (query: String, offset: Int, limit: Int) => s"$query SKIP $offset LIMIT $limit"

  private val createCourseQuery = (course: Course) =>
    s"CREATE (:Course {name: '${course.title}', id: '${course.id.get}', preview: '${course.preview}'})"
}
