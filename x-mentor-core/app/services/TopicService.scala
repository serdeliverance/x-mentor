package services

import global.ApplicationResult
import models.Topic
import play.api.Logging
import repositories.graph.TopicRepository

import javax.inject.{Inject, Singleton}

@Singleton
class TopicService @Inject()(topicRepository: TopicRepository) extends Logging {

  /**
   * Retrieves all topics from redisGraph
   *
   */
  def getAll(): ApplicationResult[List[Topic]] =
    topicRepository.getTopics()
}
