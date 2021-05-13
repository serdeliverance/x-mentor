package services

import akka.Done
import akka.Done.done
import global.ApplicationResult
import models.events._
import models.{Course, Interest, Rating, StudentProgress}
import play.api.Logging
import services.SseService.CourseCreatedSseEvent
import streams._
import util.{ApplicationResultUtils, MapMarkerContext}

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NotificationService @Inject()(
    messagePublisher: MessagePublisher,
    sseService: SseService
  )(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with Logging {

  /**
    * Send the course creation message to redis streams. It also sends a message to SSE stream (Akka Stream source) in order
    * to notify SSE subscribed users.
    *
    */
  def notifyCourseCreation(course: Course)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $course to $COURSE_CREATION_STREAM stream")
    sseService.pushEvent(CourseCreatedSseEvent(course, LocalDateTime.now))
    messagePublisher.publishEvent(COURSE_CREATION_STREAM, CourseCreated(course.title, course.topic))
  }

  def notifyRating(rating: Rating)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $rating to $COURSE_RATED_STREAM stream")
    messagePublisher.publishEvent(COURSE_RATED_STREAM, CourseRated(rating.student, rating.course, rating.stars))
  }

  def notifyInterest(interest: Interest)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $interest to $STUDENT_INTEREST_STREAM stream")
    messagePublisher.publishEvent(STUDENT_INTEREST_STREAM, StudentInterested(interest.student, interest.topic))
  }

  def notifyInterestLost(interest: Interest)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $interest to $LOST_INTEREST_STREAM stream")
    messagePublisher.publishEvent(LOST_INTEREST_STREAM, LostInterest(interest.student, interest.topic))
  }

  def notifyStudentProgress(
      studentProgress: StudentProgress
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Done] = {
    logger.info(s"Sending message: $studentProgress to $STUDENT_PROGRESS_STREAM stream")
    messagePublisher.publishEvent(STUDENT_PROGRESS_STREAM,
                                  StudentProgressRegistered(studentProgress.student, studentProgress.progress))
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
