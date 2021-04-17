package services

import akka.Done
import cats.data.EitherT
import global.ApplicationResult
import models.Rating
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import cats.implicits._
import models.converters.RatingConverter.RatingConverter
import repositories.RedisGraphRepository

@Singleton
class RatingService @Inject()(
    redisGraphRepository: RedisGraphRepository,
    notificationService: NotificationService
  )(implicit ec: ExecutionContext)
    extends Logging {

  def rate(rating: Rating): ApplicationResult[Done] = {
    for {
      _ <- EitherT { redisGraphRepository.createRatesRelation(rating) }
      _ <- EitherT { notificationService.notifyRating(rating.toMsg()) }
    } yield Done
  }.value
}
