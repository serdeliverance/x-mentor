package services

import akka.Done
import akka.Done.done
import cats.data.EitherT
import cats.implicits._
import global.{ApplicationResult, ApplicationResultExtended}
import models.Rating
import models.errors.InvalidOperationError
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

  def rate(rating: Rating): ApplicationResult[Done] = {
    for {
      _ <- EitherT { validateIsEnrolled(rating.student, rating.course) }
      _ <- EitherT { validateIsNotRated(rating.student, rating.course) }
      _ <- EitherT { redisGraphRepository.createRatesRelation(rating) }
      _ <- EitherT { notificationService.notifyRating(rating) }
    } yield Done
  }.value

  private def validateIsEnrolled(student: String, course: String): ApplicationResult[Done] =
    redisGraphRepository.existsStudyingRelation(student, course).innerMap {
      case true => Right(done())
      case false => {
        logger.info(s"Validation failed: $student is not enrolled in $course course")
        Left(InvalidOperationError(s"student: $student is not enrolled in $course"))
      }
    }

  private def validateIsNotRated(student: String, course: String): ApplicationResult[Done] =
    redisGraphRepository.existsRatesRelation(student, course).innerMap {
      case true => {
        logger.info(s"Validation failed: $student has already rated $course")
        Left(InvalidOperationError(s"$student has already rated $course"))
      }
      case false => Right(done())
    }
}
