package models

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import models.json.CirceImplicits

case class Course(id: Long, title: String, description: String, content: String, preview: String, topic: String, rating: Int)
