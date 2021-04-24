package services

import akka.Done
import akka.Done.done
import cats.data.EitherT
import constants.{COURSE_IDS_FILTER, COURSE_KEY, COURSE_LAST_ID_KEY}
import global.{ApplicationResult, ApplicationResultExtended, EitherResult}
import io.rebloom.client.Client
import io.redisearch.{Document, Query}

import javax.inject.{Inject, Singleton}
import models.{Course, CourseResponse}
import models.errors.{EmptyResponse, NotFoundError, UnexpectedError}
import play.api.Logging
import redis.clients.jedis.Jedis
import redis.clients.jedis.util.Pool
import repositories.{RediSearchRepository, RedisGraphRepository, RedisJsonRepository, RedisRepository}
import cats.implicits._
import io.circe.parser.decode
import util.{ApplicationResultUtils, CourseConverter, JsonParsingUtils, RedisJsonUtils}

import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CourseService @Inject()(
    redisBloom: Client,
    redisJsonRepository: RedisJsonRepository,
    redisRepository: RedisRepository,
    redisGraphRepository: RedisGraphRepository,
    redisPool: Pool[Jedis],
    rediSearchRepository: RediSearchRepository
  )(implicit ec: ExecutionContext)
    extends Logging
    with JsonParsingUtils
    with ApplicationResultUtils
    with RedisJsonUtils {

  def create(course: Course): ApplicationResult[Done] =
    ApplicationResult {
      val redisInstance      = redisPool.getResource
      val currentIndex: Long = redisInstance.get(COURSE_LAST_ID_KEY).toLong + 1
      val key                = s"$COURSE_KEY$currentIndex"
      val updatedCourse      = course.copy(id = Some(currentIndex))

      logger.info(s"Storing course $currentIndex in Redis, increasing last id and adding to bloom filter")
      // Insert into redisJSON
      redisJsonRepository.set(key, CourseConverter.courseToMap(updatedCourse).asJava)
      redisInstance.incr(COURSE_LAST_ID_KEY)
      // Insert into bloom filter
      redisBloom.add(COURSE_IDS_FILTER, currentIndex.toString)
    }.map(_ => Right(done()))

  def enroll(courseId: Long): ApplicationResult[Done] = ???

  def retrieve(q: String, page: Int): ApplicationResult[CourseResponse] = {
    val itemsPerPage = 6
    val offset       = (page - 1) * itemsPerPage
    val queryString  = if (q.isEmpty) "*" else s"$q*"
    logger.info(s"Retrieving courses with query $queryString and offset $offset")
    val query = new Query(queryString).limit(offset, offset + itemsPerPage)
    for {
      coursesResp <- EitherT { rediSearchRepository.search(query) }
      courseList  <- EitherT { handleSearchResp(coursesResp) }
    } yield courseList
  }.value

  // TODO add pagination
  def getCoursesByStudent(student: String): ApplicationResult[CourseResponse] = {
    for {
      coursesFromGraph <- EitherT { redisGraphRepository.getCoursesByStudent(student) }
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

  def retrieveById(courseId: Long): ApplicationResult[Course] =
    if (redisBloom.exists(COURSE_IDS_FILTER, courseId.toString)) {
      redisJsonRepository.get[Course](s"$COURSE_KEY$courseId")
    } else {
      ApplicationResult.error(NotFoundError("Course not found"))
    }
}
