package services

import akka.Done
import global.ApplicationResult
import io.rebloom.client.Client
import javax.inject.{Inject, Singleton}
import play.api.Logging

import scala.concurrent.ExecutionContext

@Singleton
class CourseService @Inject()(
   redisBloom: Client
 )(implicit ec: ExecutionContext)
  extends Logging {

  def create(): ApplicationResult[Done] = ???

  def enroll(): ApplicationResult[Done] = ???

  def retrieveAll(): ApplicationResult[Done] = ???

  def retrieveById(): ApplicationResult[Done] = ???
}
