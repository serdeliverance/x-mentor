package services

import akka.Done
import akka.Done.done
import cats.data.EitherT
import cats.implicits._
import global.{ApplicationResult, ApplicationResultExtended}
import models.Rating
import models.errors.InvalidOperationError
import play.api.Logging
import repositories.graph.RelationsRepository
import util.MapMarkerContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RatingService @Inject()(
    relationsRepository: RelationsRepository,
    notificationService: NotificationService
  )(implicit ec: ExecutionContext)
    extends Logging {

  /**
   * Rates a specific course
   *
   * 1. Verifies if a studying relation exists between the student and the course
   * 2. Verifies that a rates relation does not exists between the student and the course
   * 3. Creates the rates relation between the student and the course
   * 4. Publishes [[streams.COURSE_RATED_STREAM]] event
   *
   */
  def rate(rating: Rating)(implicit mmc: MapMarkerContext): ApplicationResult[Done] = {
    for {
      _ <- EitherT { validateIsEnrolled(rating.student, rating.course) }
      _ <- EitherT { validateIsNotRated(rating.student, rating.course) }
      _ <- EitherT { relationsRepository.createRatesRelation(rating) }
      _ <- EitherT { notificationService.notifyRating(rating) }
    } yield Done
  }.value

  private def validateIsEnrolled(
      student: String,
      course: String
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Done] =
    relationsRepository.existsStudyingRelation(student, course).innerMap {
      case true => Right(done())
      case false => {
        logger.info(s"Validation failed: $student is not enrolled in $course course")
        Left(InvalidOperationError(s"student: $student is not enrolled in $course"))
      }
    }

  private def validateIsNotRated(
      student: String,
      course: String
    )(implicit mmc: MapMarkerContext
    ): ApplicationResult[Done] =
    relationsRepository.existsRatesRelation(student, course).innerMap {
      case true => {
        logger.info(s"Validation failed: $student has already rated $course")
        Left(InvalidOperationError(s"$student has already rated $course"))
      }
      case false => Right(done())
    }
}
