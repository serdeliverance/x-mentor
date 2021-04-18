package models.events

sealed trait DomainEvent

// TODO add date of type LocalDateTime
case class CourseRated(student: String, course: String, stars: Int) extends DomainEvent
