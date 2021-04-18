package services

import akka.Done
import cats.data.EitherT
import cats.implicits._
import global.ApplicationResult
import models.Rating
import play.api.Logging
import repositories.RedisGraphRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RatingService @Inject()(
    redisGraphRepository: RedisGraphRepository,
    notificationService: NotificationService
  )(implicit ec: ExecutionContext)
    extends Logging {

  // TODO add student validation
  def rate(rating: Rating): ApplicationResult[Done] = {
    for {
      _ <- EitherT { redisGraphRepository.createRatesRelation(rating) }
      _ <- EitherT { notificationService.notifyRating(rating) }
    } yield Done
  }.value
}
