package services

import akka.Done
import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.messages.RecommendationMessage
import play.api.Logging
import repositories.RedisGraphRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationService @Inject()(
    redisGraphRepository: RedisGraphRepository,
    notificationService: NotificationService
  )(implicit ec: ExecutionContext)
    extends Logging {

  def recommend(userId: Long, topicId: Option[Long], courseId: Option[Long]): ApplicationResult[Done] = {
    for {
      _ <- EitherT { redisGraphRepository.createRecommendRelation(userId, topicId, courseId) }
      _ <- EitherT { notificationService.notifyRecommendation(RecommendationMessage(userId, topicId, courseId)) }
    } yield Done
  }.value
}
