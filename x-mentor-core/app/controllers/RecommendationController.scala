package controllers

import controllers.circe.Decodable
import controllers.converters.ErrorToResultConverter
import models.dtos.requests.{CourseRecommendationRequestDTO, TopicRecommendationRequestDTO}
import play.api.Logging
import play.api.mvc.{Action, BaseController, ControllerComponents}
import services.RecommendationService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RecommendationController @Inject()(
    val controllerComponents: ControllerComponents,
    recommendationService: RecommendationService
  )(implicit ec: ExecutionContext)
    extends BaseController
    with Decodable
    with ErrorToResultConverter
    with Logging {

  def recommendCourse(): Action[CourseRecommendationRequestDTO] = Action.async(decode[CourseRecommendationRequestDTO]) {
    request =>
      // TODO remove hardcoded variable
      val userId   = 1
      val courseId = request.body.courseId
      logger.info(s"Receiving course recommendation. user: $userId recommends course: $courseId")
      recommendationService
        .recommend(userId, None, Some(courseId))
        .map(_ => Created)
  }

  def recommendTopic(): Action[TopicRecommendationRequestDTO] = Action.async(decode[TopicRecommendationRequestDTO]) {
    request =>
      // TODO remove hardcoded variable
      val userId  = 1
      val topicId = request.body.topicId
      logger.info(s"Receiving topic recommendation. user: $userId recommends topic: $topicId")
      recommendationService
        .recommend(userId, Some(topicId), None)
        .map(_ => Created)
  }
}
