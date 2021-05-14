package services

import cats.data.EitherT
import cats.implicits._
import constants.STUDENT_PROGRESS_LIST_KEY
import global.ApplicationResult
import models.StudentProgress
import models.dtos.responses.LeadersBoardDTO
import play.api.Logging
import repositories.{RedisRepository, RedisTimeSeriesRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LeaderboardService @Inject()(
    redisRepository: RedisRepository,
    redisTimeSeriesRepository: RedisTimeSeriesRepository
  )(implicit ec: ExecutionContext)
    extends Logging {

  private val LEADERS_COUNT = 5

  /**
    * Retrieves a student progress list ordered by progress time (watching time) desc. It will be
    * used to show the leader board in the UI. It uses a time window of three months for evaluating student progress
    *
    * 1. It looks the student progress list key in order to get the keys for looking into timeseries
    * 2. Foreach of them, it reads the timeseries db performing sum aggregation
    * 3. Order desc and return
    *
    */
  def get(): ApplicationResult[LeadersBoardDTO] = {
    for {
      studentProgressKeys <- EitherT { redisRepository.listAll(STUDENT_PROGRESS_LIST_KEY) }
      studentProgress <- EitherT {
        redisTimeSeriesRepository.forAllThreeMonthsRangeSummarized[StudentProgress](studentProgressKeys)
      }
      leaders = studentProgress.sortBy(_.progress).reverse.take(LEADERS_COUNT)
    } yield LeadersBoardDTO(leaders)
  }.value
}
