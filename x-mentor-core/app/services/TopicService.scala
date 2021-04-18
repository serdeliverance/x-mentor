package services

import global.ApplicationResult
import models.Topic
import play.api.Logging
import repositories.RedisGraphRepository

import javax.inject.{Inject, Singleton}

@Singleton
class TopicService @Inject()(redisGraphRepository: RedisGraphRepository) extends Logging {

  def getAll(): ApplicationResult[List[Topic]] =
    redisGraphRepository.getTopics()
}
