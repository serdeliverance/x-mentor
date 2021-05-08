package services

import akka.Done
import global.ApplicationResult
import models.StudentProgress
import util.MapMarkerContext

import javax.inject.{Inject, Singleton}

@Singleton
class MetricsService @Inject()(notificationService: NotificationService) {

  def registerStudentProgress(student: String, minutes: Int)(implicit mmc: MapMarkerContext): ApplicationResult[Done] =
    notificationService.notifyStudentProgress(StudentProgress(student, minutes))

}
