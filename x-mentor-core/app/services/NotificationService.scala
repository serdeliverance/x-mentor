package services

import akka.Done
import akka.Done.done
import global.ApplicationResult
import models.{Interest, Rating}
import models.events.{CourseRated, LostInterest, StudentInterested}
import play.api.Logging
import streams.{COURSE_RATED_STREAM, LOST_INTEREST_STREAM, MessagePublisher, STUDENT_INTEREST_STREAM}
import util.ApplicationResultUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationService @Inject()(messagePublisher: MessagePublisher)(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with Logging {

  def notifyRating(rating: Rating): ApplicationResult[Done] = {
    logger.info(s"Sending message: $rating to $COURSE_RATED_STREAM")
    messagePublisher.publishEvent(COURSE_RATED_STREAM, CourseRated(rating.student, rating.course, rating.stars))
  }

  def notifyInterest(interest: Interest): ApplicationResult[Done] = {
    logger.info(s"Sending message: $interest to $STUDENT_INTEREST_STREAM")
    messagePublisher.publishEvent(STUDENT_INTEREST_STREAM, StudentInterested(interest.student, interest.topic))
  }

  def notifyInterestLost(interest: Interest): ApplicationResult[Done] = {
    logger.info(s"Sending message: $interest to $LOST_INTEREST_STREAM")
    messagePublisher.publishEvent(LOST_INTEREST_STREAM, LostInterest(interest.student, interest.topic))
  }

  def notifyInterestInBulk(interests: Seq[Interest]): ApplicationResult[Done] =
    sequence {
      interests.map(interest => notifyInterest(interest))
    }.map(_ => Right(done()))

  def notifyInterestLostInBulk(interests: Seq[Interest]): ApplicationResult[Done] =
    sequence {
      interests.map(interest => notifyInterestLost(interest))
    }.map(_ => Right(done()))
}
