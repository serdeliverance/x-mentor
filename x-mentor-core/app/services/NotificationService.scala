package services

import akka.Done
import akka.Done.done
import global.ApplicationResult
import models.configurations.SSEConfiguration
import models.{Course, Interest, Rating}
import models.events.{CourseCreated, CourseRated, LostInterest, StudentInterested}
import play.api.Logging
import streams.{
  COURSE_CREATION_STREAM,
  COURSE_RATED_STREAM,
  LOST_INTEREST_STREAM,
  MessagePublisher,
  STUDENT_INTEREST_STREAM
}
import util.{ApplicationResultUtils, MapMarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationService @Inject()(
    messagePublisher: MessagePublisher,
    sseConfiguration: SSEConfiguration
  )(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with Logging {

  def notifyCourseCreation(course: Course)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $course to $COURSE_CREATION_STREAM")
    sseConfiguration.notificationActor ! "course created"
    messagePublisher.publishEvent(COURSE_CREATION_STREAM, CourseCreated(course.title, course.topic))
  }

  def notifyRating(rating: Rating)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $rating to $COURSE_RATED_STREAM")
    messagePublisher.publishEvent(COURSE_RATED_STREAM, CourseRated(rating.student, rating.course, rating.stars))
  }

  def notifyInterest(interest: Interest)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $interest to $STUDENT_INTEREST_STREAM")
    messagePublisher.publishEvent(STUDENT_INTEREST_STREAM, StudentInterested(interest.student, interest.topic))
  }

  def notifyInterestLost(interest: Interest)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $interest to $LOST_INTEREST_STREAM")
    messagePublisher.publishEvent(LOST_INTEREST_STREAM, LostInterest(interest.student, interest.topic))
  }

  def notifyInterestInBulk(interests: Seq[Interest])(implicit mmc: MapMarkerContext): ApplicationResult[Done] =
    sequence {
      interests.map(interest => notifyInterest(interest))
    }.map(_ => Right(done()))

  def notifyInterestLostInBulk(interests: Seq[Interest])(implicit mmc: MapMarkerContext): ApplicationResult[Done] =
    sequence {
      interests.map(interest => notifyInterestLost(interest))
    }.map(_ => Right(done()))

}
