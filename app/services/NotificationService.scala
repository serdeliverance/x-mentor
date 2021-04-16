package services

import akka.Done
import global.ApplicationResult
import models.messages.{RatingMessage, RecommendationMessage}
import play.api.Logging
import queues.{COURSE_RATED_EVENT, COURSE_RECOMMENDED_EVENT, MessagePublisher}

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationService @Inject()(messagePublisher: MessagePublisher) extends Logging {

  def notifyRating(ratingMsg: RatingMessage): ApplicationResult[Done] = {
    logger.info(s"Sending $COURSE_RATED_EVENT message with data: $ratingMsg")
    messagePublisher.publish(COURSE_RATED_EVENT, ratingMsg)
  }

  def notifyRecommendation(recommendationMsg: RecommendationMessage): ApplicationResult[Done] = {
    logger.info(s"Sending $COURSE_RECOMMENDED_EVENT message with data: $recommendationMsg")
    messagePublisher.publish(COURSE_RECOMMENDED_EVENT, recommendationMsg)
  }
}
