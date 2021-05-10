package services

import global.ApplicationResult
import models.dtos.responses.LeadersBoardDTO
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LeadersBoardService @Inject()()(implicit ec: ExecutionContext) extends Logging {

  def get(): ApplicationResult[LeadersBoardDTO] = ???
}
