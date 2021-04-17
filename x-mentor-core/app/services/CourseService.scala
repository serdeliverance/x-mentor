package services

import akka.Done
import com.redislabs.modules.rejson.JReJSON
import global.ApplicationResult
import constans._
import io.rebloom.client.Client
import javax.inject.{Inject, Singleton}
import models.Course
import play.api.Logging

import scala.concurrent.ExecutionContext

@Singleton
class CourseService @Inject()(
   redisBloom: Client,
   redisJSON: JReJSON
 )(implicit ec: ExecutionContext)
  extends Logging {

  def create(course: Course): ApplicationResult[Done] = ???

  def enroll(courseId: Long): ApplicationResult[Done] = ???

  def retrieveAll(): ApplicationResult[Done] = ???

  def retrieveById(courseId: Long): ApplicationResult[Done] = ???
  /*{
    val exists = redisBloom.exists("courses", courseId.toString)
    if(exists){
     redisJSON.get(s"$COURSE_KEY$courseId")
    }
  }*/
}
