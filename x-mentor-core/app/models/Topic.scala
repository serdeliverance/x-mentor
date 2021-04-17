package models

// TODO analyze if id field is really needed
case class Topic(id: Option[Long], name: String, description: String)
