package services

import cats.data.EitherT
import constants.STUDENT_PROGRESS_LIST_KEY
import global.ApplicationResult
import models.dtos.responses.LeadersBoardDTO
import play.api.Logging
import repositories.{RedisRepository, RedisTimeSeriesRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import cats.implicits._
import models.dtos.responses.LeadersBoardDTO.LeaderDTO

@Singleton
class LeadersBoardService @Inject()(
    redisRepository: RedisRepository,
    redisTimeSeriesRepository: RedisTimeSeriesRepository
  )(implicit ec: ExecutionContext)
    extends Logging {

  def get(): ApplicationResult[LeadersBoardDTO] = {
    for {
      studentProgressKeys <- EitherT { redisRepository.listAll(STUDENT_PROGRESS_LIST_KEY) }
      leaderBoards        <- EitherT { redisTimeSeriesRepository.getAll(studentProgressKeys) }
    } yield LeadersBoardDTO(List.empty[LeaderDTO])
  }.value
}
