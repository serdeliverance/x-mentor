package services

import akka.{Done, NotUsed}
import akka.Done.done
import akka.actor.ActorSystem
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.QueueOfferResult.{Dropped, Enqueued, Failure, QueueClosed}
import akka.stream.scaladsl.{Source, SourceQueue, SourceQueueWithComplete}
import global.ApplicationResult
import models.{Course, Interest, Notification, Rating}
import models.events.{CourseCreated, CourseRated, LostInterest, StudentInterested}
import play.api.Logging
import streams.{COURSE_CREATION_STREAM, COURSE_RATED_STREAM, LOST_INTEREST_STREAM, MessagePublisher, STUDENT_INTEREST_STREAM}
import util.{ApplicationResultUtils, MapMarkerContext}
import javax.inject.{Inject, Singleton}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationService @Inject()(messagePublisher: MessagePublisher)(implicit ec: ExecutionContext)
    extends ApplicationResultUtils
    with Logging {

  def notifyCourseCreation(course: Course)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    logger.info(s"Sending message: $course to $COURSE_CREATION_STREAM")
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


  implicit private val system: ActorSystem = ActorSystem("Notification")
  implicit private val materializer: Materializer = Materializer(system)
  val sourceOfNotifications = mutable.Queue.empty[Source[Notification, NotUsed]]
  private val sourceQueueOfNotifications2 =  mutable.Queue.empty[SourceQueue[Notification]]

  def getSourceOfNotifications: Source[Notification, NotUsed] = {
    logger.info("Retreiving notifications")
    sourceOfNotifications.dequeue()
  }

  // it's configured to keep the source open for 30s (reusing it for consecutive requests)
  def createSourceOfNotifications(): Source[Notification, NotUsed] = {
    val initialSourceOfNotifications = Source.queue[Notification](1000, OverflowStrategy.dropHead)
    val sourceQueueOfNotifications: (SourceQueue[Notification], Source[Notification, NotUsed]) =
      initialSourceOfNotifications.preMaterialize()
    sourceOfNotifications.append(sourceQueueOfNotifications._2)
    sourceQueueOfNotifications2.append(sourceQueueOfNotifications._1)
    // watch for closing the source queue (probably because of being inactive)
    sourceQueueOfNotifications._1.watchCompletion().foreach { _ =>
      logger.info("Completed")
    }
    sourceQueueOfNotifications._2
  }

  def registerNewNotification(notification: Notification): Future[Unit] = {
    createSourceOfNotifications()
    sourceQueueOfNotifications2.dequeue().offer(notification) .flatMap {
      case Enqueued =>
        Future.successful(())
      case Dropped =>
        Future.failed(new Exception(s"Notification couldn't be published"))
      case Failure(e) =>
        println("Failure(e):" + e.getMessage)
        Future.failed(new Exception(s"Notification couldn't be published because of $e"))
      case QueueClosed =>
        println("QueueClosed")
        Future.failed(new Exception(s"Notification couldn't be published because the publishing queue was closed"))
    }.recover({
      case t =>
        println(t.getMessage)
    })
  }
}