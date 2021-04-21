package util

import models.Course

object CourseConverter {

  def courseToMap(course: Course): Map[String, Any] = Map(
    "id" -> course.id.orNull,
    "title" -> course.title,
    "description" -> course.description,
    "preview" -> course.preview,
    "content" -> course.content,
    "topic" -> course.topic,
    "rating" -> course.rating.orNull
  )
}
