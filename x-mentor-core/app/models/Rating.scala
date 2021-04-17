package models

case class Rating(id: Option[Long] = None, studentId: Long, courseId: Long, stars: Int)
