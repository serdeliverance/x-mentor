package models

case class Course(id: Option[Long] = None, title: String, description: String, content: String, preview: String, topic: String, rating: Option[Int] = None)
