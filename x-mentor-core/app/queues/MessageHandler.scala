package queues

import akka.actor.{Actor, Props}
import io.circe.Decoder
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import models.json.CirceImplicits
import play.api.Logging
import queues.MessageHandler.{CourseRated, RecommendationCreated}
import repositories.RedisGraphRepository

import javax.inject.Inject

class MessageHandler @Inject()(redisGraphRepository: RedisGraphRepository) extends Actor with Logging {

  // TODO fix logging and replace printlns
  override def receive: Receive = {
    case CourseRated(studentId, courseId, stars) =>
      logger.info("Received courseRated")
      println(s"Received event: $COURSE_RATED_EVENT with data: $studentId")
      if (stars > 3) {
        redisGraphRepository.recommendCourse(studentId, courseId)
      }
    case RecommendationCreated(studentId, topicId, courseId) =>
      println(s"Received recommendation. studentId: $studentId, topicId: $topicId, courseId: $courseId")
      (topicId, courseId) match {
        case (Some(topicId), None)  => redisGraphRepository.recommendTopic(studentId, topicId)
        case (None, Some(courseId)) => redisGraphRepository.recommendCourse(studentId, courseId)
        case _                      => logger.debug("Invalid recommendation message received")
      }
  }
}

object MessageHandler extends CirceImplicits {

  def props(redisGraphRepository: RedisGraphRepository) =
    Props[MessageHandler](new MessageHandler(redisGraphRepository))

  // protocol
  sealed trait Command
  case class CourseRated(studentId: Long, courseId: Long, stars: Int)                              extends Command
  case class RecommendationCreated(studentId: Long, topicId: Option[Long], courseId: Option[Long]) extends Command

  // json
  implicit lazy val courseRatedDecoder: Decoder[CourseRated] = deriveConfiguredDecoder
}
