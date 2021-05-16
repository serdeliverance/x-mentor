package services

import akka.Done
import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.{Interest, Topic}
import play.api.Logging
import repositories.graph.{RelationsRepository, TopicRepository}
import util.{ApplicationResultUtils, MapMarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InterestService @Inject()(
    topicRepository: TopicRepository,
    relationsRepository: RelationsRepository,
    notificationService: NotificationService
  )(implicit ec: ExecutionContext)
    extends Logging
    with ApplicationResultUtils {

  /**
   * Creates a relation between a student and different topics
   *
   * 1. Gets all interested relations from redisGraph
   * 2. Gets difference between already existed relations and new ones
   * 3. Creates new interested relations into redisGraph
   * 4. Removes interested relations that don't apply anymore
   * 5. Publishes [[streams.LOST_INTEREST_STREAM]] and [[streams.STUDENT_INTEREST_STREAM]] events
   *
   */
  def registerInterest(
      student: String,
      interests: List[Interest]
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Done] = {
    for {
      persistedTopicsOfInterest <- EitherT { topicRepository.getInterestTopicsByStudent(student) }
      persistedInterests        <- EitherT { mapToInterest(student, persistedTopicsOfInterest) }
      interestsToAdd            <- EitherT { difference(interests, persistedInterests) }
      _                         <- EitherT { relationsRepository.createInterestRelationInBulk(interestsToAdd) }
      interestsToDelete         <- EitherT { difference(persistedInterests, interests) }
      _                         <- EitherT { relationsRepository.removeInterestRelationInBulk(interestsToDelete) }
      _                         <- EitherT { notificationService.notifyInterestLostInBulk(interestsToDelete) }
      _                         <- EitherT { notificationService.notifyInterestInBulk(interestsToAdd) }
    } yield Done
  }.value

  /**
   * Retrieves all interested relations between a student and different topics from redisGraph
   *
   */
  def getInterests(student: String): ApplicationResult[List[Topic]] =
    topicRepository.getInterestTopicsByStudent(student)

  private def mapToInterest(student: String, topics: List[Topic]): ApplicationResult[List[Interest]] =
    ApplicationResult(topics.map(topic => Interest(student, topic.name)))
}
