package models.events

import java.time.Instant

sealed trait DomainEvent

case class CourseCreated(title: String, topic: String, timestamp: Long = Instant.now.getEpochSecond) extends DomainEvent

case class CourseRated(student: String, course: String, stars: Int, timestamp: Long = Instant.now.getEpochSecond)
    extends DomainEvent

case class StudentInterested(student: String, topic: String, timestamp: Long = Instant.now.getEpochSecond)
    extends DomainEvent

case class LostInterest(student: String, topic: String, timestamp: Long = Instant.now.getEpochSecond)
    extends DomainEvent

case class StudentProgressRegistered(student: String, duration: Int, timestamp: Long = Instant.now.getEpochSecond)
    extends DomainEvent
