package services

import akka.Done
import global.ApplicationResult
import play.api.Logging

import javax.inject.{Inject, Singleton}

@Singleton
class InterestService @Inject()() extends Logging {

  def interest(student: String, interests: List[String]): ApplicationResult[Done] = ???
}
