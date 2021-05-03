package repositories.graph

import akka.Done
import akka.Done.done
import global.{ApplicationResult, ApplicationResultExtended}
import models.configurations.RedisGraphConfiguration
import models.{Has, Interest, Rating, Studying}
import play.api.Logging
import repositories.RedisGraphRepository
import repositories.graph.RelationsRepository._
import util.{ApplicationResultUtils, MapMarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RelationsRepository @Inject()(
    redisGraphRepository: RedisGraphRepository,
    courseRepository: CourseRepository
  )(implicit redisGraphConfiguration: RedisGraphConfiguration,
    ec: ExecutionContext)
    extends Logging
    with ApplicationResultUtils {

  def createRatesRelation(rating: Rating): ApplicationResult[Done] =
    redisGraphRepository.executeCreateQuery(createRatesQuery(rating))

  def createHasRelation(hasRelation: Has): ApplicationResult[Done] =
    redisGraphRepository.executeCreateQuery(createHasRelationQuery(hasRelation))

  def createInterestRelation(interest: Interest): ApplicationResult[Done] =
    redisGraphRepository.executeCreateQuery(createInterestRelationQuery(interest))

  def createStudyingRelation(studying: Studying): ApplicationResult[Done] =
    redisGraphRepository.executeCreateQuery(createStudyingRelationQuery(studying))

  def removeInterestRelation(interest: Interest): ApplicationResult[Done] =
    redisGraphRepository.executeCreateQuery(deleteInterestRelationQuery(interest))

  def createInterestRelationInBulk(
      interests: Seq[Interest]
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Done] = {
    logger.info(s"Adding interests relations: ${interests.map(_.topic)}")
    sequence {
      interests.map(interest => createInterestRelation(interest))
    }.map(_ => Right(done()))
  }

  def removeInterestRelationInBulk(
      interests: Seq[Interest]
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Done] = {
    logger.info(s"Removing interests relations: ${interests.map(_.topic)}")
    sequence {
      interests.map(interest => removeInterestRelation(interest))
    }.map(_ => Right(done()))
  }

  def existsStudyingRelation(student: String, course: String): ApplicationResult[Boolean] =
    courseRepository.getCoursesByStudent(student).innerMap(result => Right(result.exists(_.name == course)))

  def existsRatesRelation(student: String, course: String): ApplicationResult[Boolean] =
    courseRepository.getCoursesRatedByStudent(student).innerMap(result => Right(result.exists(_.name == course)))
}

object RelationsRepository {

  private val createInterestRelationQuery = (interest: Interest) =>
    s"MATCH (s:Student), (t:Topic) WHERE s.username = '${interest.student}' AND t.name = '${interest.topic}' CREATE (s)-[:interested]->(t)"

  private val createHasRelationQuery = (hasRelation: Has) =>
    s"MATCH (t:Topic), (c:Course) WHERE t.name = '${hasRelation.topic}' AND c.name = '${hasRelation.course}' CREATE (t)-[:has]->(c)"

  private val createRatesQuery = (rating: Rating) =>
    s"MATCH (s:Student), (c:Course) WHERE s.username = '${rating.student}' AND c.name = '${rating.course}' CREATE (s)-[:rates {rating:${rating.stars}}]->(c)"

  private val createStudyingRelationQuery = (studying: Studying) =>
    s"MATCH (s:Student), (c:Course) WHERE s.username = '${studying.student}' AND c.name = '${studying.course}' CREATE (s)-[:studying]->(c)"

  private val deleteInterestRelationQuery = (interest: Interest) =>
    s"MATCH (student)-[interest:interested]->(topic) WHERE student.username='${interest.student}' and topic.name='${interest.topic}' DELETE interest"
}
