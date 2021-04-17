package models

case class Recommendation(id: Option[Long], userId: Long, topicId: Option[Long], courseId: Option[Long])
