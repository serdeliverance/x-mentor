package models.converters

import models.Rating
import models.messages.RatingMessage

object RatingConverter {
  implicit class RatingConverter(rating: Rating) {
    def toMsg(): RatingMessage = RatingMessage(rating.studentId, rating.courseId, rating.stars)
  }
}
