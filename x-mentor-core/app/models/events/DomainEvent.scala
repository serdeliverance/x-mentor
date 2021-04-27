package models.events

import java.time.LocalDateTime

sealed trait DomainEvent

// TODO add date of type LocalDateTime
case class CourseRated(student: String, course: String, stars: Int) extends DomainEvent
case class InterestRegistered(student: String, topic: String, date: LocalDateTime = LocalDateTime.now)
    extends DomainEvent
