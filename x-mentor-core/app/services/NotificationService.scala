package services

import akka.Done
import akka.Done.done
import global.ApplicationResult
import models.{Interest, Rating}
import models.events.{CourseRated, StudentInterested}
import play.api.Logging
import streams.{COURSE_RATED_STREAM, MessagePublisher, STUDENT_INTEREST_STREAM}
import util.ApplicationResultUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationService @Inject()(messagePublisher: MessagePublisher)(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with Logging {

  def notifyRating(rating: Rating): ApplicationResult[Done] = {
    logger.info(s"Sending $COURSE_RATED_STREAM message with data: $rating")
    messagePublisher.publishEvent(COURSE_RATED_STREAM, CourseRated(rating.student, rating.course, rating.stars))
  }

  def notifyInterest(interest: Interest): ApplicationResult[Done] = {
    logger.info(s"Sending $STUDENT_INTEREST_STREAM message with data: $interest")
    messagePublisher.publishEvent(STUDENT_INTEREST_STREAM, StudentInterested(interest.student, interest.topic))
  }

  def notifyInterestInBulk(interests: List[Interest]): ApplicationResult[Done] =
    sequence {
      interests.map(interest => notifyInterest(interest))
    }.map(_ => Right(done()))
}
