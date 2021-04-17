package services

import akka.Done
import global.ApplicationResult
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.ExecutionContext

@Singleton
class CourseService @Inject()(
  )(implicit ec: ExecutionContext)
    extends Logging {

  def create(): ApplicationResult[Done] = ???

  def enroll(): ApplicationResult[Done] = ???

}
