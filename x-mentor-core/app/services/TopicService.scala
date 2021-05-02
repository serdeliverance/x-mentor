package services

import global.ApplicationResult
import models.Topic
import play.api.Logging
import repositories.graph.TopicRepository

import javax.inject.{Inject, Singleton}

@Singleton
class TopicService @Inject()(topicRepository: TopicRepository) extends Logging {

  def getAll(): ApplicationResult[List[Topic]] =
    topicRepository.getTopics()
}
