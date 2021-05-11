package services

import akka.Done
import global.ApplicationResult
import models.StudentProgress
import util.MapMarkerContext

import javax.inject.{Inject, Singleton}

@Singleton
class MetricsService @Inject()(notificationService: NotificationService) {

  /**
    * Sends student progress registered (Domain Event) to redis streams. That message will be consumed by gears who then will feed
    * the timeseries database
    *
    */
  def registerStudentProgress(student: String, seconds: Int)(implicit mmc: MapMarkerContext): ApplicationResult[Done] =
    notificationService.notifyStudentProgress(StudentProgress(student, seconds))

}
