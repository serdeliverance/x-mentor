package services

import akka.Done
import cats.data.EitherT
import constants.{COURSE_IDS_FILTER, COURSE_KEY, COURSE_LAST_ID_KEY, ITEMS_PER_PAGE, USERS_FILTER}
import global.{ApplicationResult, ApplicationResultExtended}
import io.redisearch.{Document, Query}
import javax.inject.{Inject, Singleton}
import models.{Course, CourseResponse, Studying}
import models.errors.EmptyResponse
import play.api.Logging
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool
import repositories.{
  RediSearchRepository,
  RedisBloomRepository,
  RedisGraphRepository,
  RedisJsonRepository,
  RedisRepository
}
import cats.implicits._
import io.circe.parser.decode
import util.{ApplicationResultUtils, CourseConverter, JsonUtils, RedisJsonUtils}

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext

@Singleton
class CourseService @Inject()(
    redisJsonRepository: RedisJsonRepository,
    redisRepository: RedisRepository,
    redisGraphRepository: RedisGraphRepository,
    redisPool: Pool[Jedis],
    rediSearchRepository: RediSearchRepository,
    redisBloomRepository: RedisBloomRepository
  )(implicit ec: ExecutionContext)
    extends Logging
    with JsonUtils
    with ApplicationResultUtils
    with RedisJsonUtils {

  def create(course: Course): ApplicationResult[Done] = {
      val redisInstance      = redisPool.getResource
      val currentIndex: Long = redisInstance.get(COURSE_LAST_ID_KEY).toLong + 1
      val key                = s"$COURSE_KEY$currentIndex"
      val updatedCourse      = course.copy(id = Some(currentIndex))

      logger.info(s"Storing course $currentIndex in Redis, increasing last id and adding to bloom filter")
      redisInstance.incr(COURSE_LAST_ID_KEY)

      for {
        _ <- EitherT(redisJsonRepository.set(key, CourseConverter.courseToMap(updatedCourse).asJava))
        _ <- EitherT(redisBloomRepository.add(COURSE_IDS_FILTER, currentIndex.toString))
      } yield Done
    }.value

  def enroll(courseId: Long, username: String): ApplicationResult[Done] = {
    logger.info(s"Enrolling user $username in course $courseId")
    for {
      _      <- EitherT(redisBloomRepository.exists(USERS_FILTER, username))
      course <- EitherT(retrieveById(courseId))
      _      <- EitherT(redisGraphRepository.createStudyingRelation(Studying(username, course.title)))
    } yield Done
  }.value

  def retrieve(q: String, page: Int): ApplicationResult[CourseResponse] = {
    val offset      = (page - 1) * ITEMS_PER_PAGE
    val queryString = if (q.isEmpty) "*" else s"$q*"
    logger.info(s"Retrieving courses with query $queryString and offset $offset")
    val query = new Query(queryString).limit(offset, offset + ITEMS_PER_PAGE)
    for {
      coursesResp <- EitherT { rediSearchRepository.search(query) }
      courseList  <- EitherT { handleSearchResp(coursesResp) }
    } yield courseList
  }.value

  def getCoursesByStudent(student: String, page: Int): ApplicationResult[CourseResponse] = {
    for {
      coursesFromGraph <- EitherT { redisGraphRepository.getCoursesByStudentPaginated(student, page) }
      courses          <- EitherT { getMultipleCoursesByName(coursesFromGraph.map(_.name)) }
    } yield CourseResponse(courses.length, courses)
  }.value

  private def getMultipleCoursesByName(courses: List[String]): ApplicationResult[Seq[Course]] = {
    val searchResults = courses
      .map(course =>
        rediSearchRepository.get(course).innerMap {
          case Some(doc) => decodeDocument(doc)
          case None      => Left(EmptyResponse)
      })
    sequence(searchResults)
  }

  private def handleSearchResp(courseResp: (Long, List[Document])): ApplicationResult[CourseResponse] =
    ApplicationResult {
      CourseResponse(
        courseResp._1,
        courseResp._2
          .map(doc => { decode[Course](formatJson(doc.getString("$"))) })
          .collect {
            case Right(decodedCourse) => decodedCourse
          }
      )
    }

  def retrieveById(courseId: Long): ApplicationResult[Course] = {
    for {
      _      <- EitherT(redisBloomRepository.exists(COURSE_IDS_FILTER, courseId.toString))
      course <- EitherT(redisJsonRepository.get[Course](s"$COURSE_KEY$courseId"))
    } yield course
  }.value
}
