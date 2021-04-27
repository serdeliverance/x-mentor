package services

import akka.Done
import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.{Interest, Student, Topic}
import play.api.Logging
import repositories.RedisGraphRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class InterestService @Inject()(redisGraphRepository: RedisGraphRepository)(implicit ec: ExecutionContext)
    extends Logging {

  def registerInterest(student: String, interests: List[Interest]): ApplicationResult[Done] = {
    for {
      persistedTopicsOfInterest <- EitherT { redisGraphRepository.getInterestTopicsByStudent(student) }
      registeredInterests       <- EitherT { mapToInterest(student, persistedTopicsOfInterest) }
      interestsToRegister       <- EitherT { filterNotRegisteredInterests(registeredInterests, interests) }
      _                         <- EitherT { redisGraphRepository.createInterestRelationInBulk(interestsToRegister) }
    } yield Done
  }.value

  private def mapToInterest(student: String, topics: List[Topic]): ApplicationResult[List[Interest]] =
    ApplicationResult(topics.map(topic => Interest(student, topic.name)))

  private def filterNotRegisteredInterests(
      registeredInterests: List[Interest],
      interestsToRegister: List[Interest]
    ): ApplicationResult[List[Interest]] = ApplicationResult {
    interestsToRegister.filterNot(interest => registeredInterests.contains(interest))
  }
}
