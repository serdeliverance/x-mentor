package services

import akka.Done
import global.ApplicationResult
import models.Rating
import models.events.CourseRated
import play.api.Logging
import streams.{COURSE_RATED_STREAM, MessagePublisher}

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationService @Inject()(messagePublisher: MessagePublisher) extends Logging {

  def notifyRating(rating: Rating): ApplicationResult[Done] = {
    logger.info(s"Sending $COURSE_RATED_STREAM message with data: $rating")
    messagePublisher.publishEvent(COURSE_RATED_STREAM, CourseRated(rating.student, rating.course, rating.stars))
  }
}
