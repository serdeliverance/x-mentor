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
class LeadersBoardService @Inject()(
    redisRepository: RedisRepository,
    redisTimeSeriesRepository: RedisTimeSeriesRepository
  )(implicit ec: ExecutionContext)
    extends Logging {

  private val LEADERS_COUNT = 5

  def get(): ApplicationResult[LeadersBoardDTO] = {
    for {
      studentProgressKeys <- EitherT { redisRepository.listAll(STUDENT_PROGRESS_LIST_KEY) }
      studentProgress     <- EitherT { redisTimeSeriesRepository.getAll[StudentProgress](studentProgressKeys) }
      leaders = studentProgress.sortBy(_.progress).reverse.take(LEADERS_COUNT)
    } yield LeadersBoardDTO(leaders)
  }.value
}
