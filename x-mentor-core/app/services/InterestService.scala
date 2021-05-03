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

  def getInterests(student: String): ApplicationResult[List[Topic]] =
    topicRepository.getInterestTopicsByStudent(student)

  private def mapToInterest(student: String, topics: List[Topic]): ApplicationResult[List[Interest]] =
    ApplicationResult(topics.map(topic => Interest(student, topic.name)))
}
