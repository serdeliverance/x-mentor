package models.events

import java.time.LocalDateTime

sealed trait DomainEvent

case class CourseRated(student: String, course: String, stars: Int, date: LocalDateTime = LocalDateTime.now)
    extends DomainEvent
case class StudentInterested(student: String, topic: String, date: LocalDateTime = LocalDateTime.now)
    extends DomainEvent
