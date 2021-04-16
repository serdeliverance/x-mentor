package jobs

import configurations.MESSAGING_DISPATCHER
import jobs.loaders.DataLoaderManager
import models.configurations.{COURSE_RATED_TOPIC, RedisConfiguration}
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import queues.MessageHandler.CourseRated
import queues.{COURSE_RATED_EVENT, ChannelListener}
import redis.clients.jedis.Jedis

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationStart @Inject()(
    lifecycle: ApplicationLifecycle,
    dataLoaderManager: DataLoaderManager,
    redisConfiguration: RedisConfiguration,
    @Named(COURSE_RATED_TOPIC) channelListener: ChannelListener[CourseRated]
  )(implicit @Named(MESSAGING_DISPATCHER) ec: ExecutionContext)
    extends Logging {

  dataLoaderManager.load()

  // TODO refactor this
  val jedis = new Jedis(redisConfiguration.host, redisConfiguration.port)

  Future {
    logger.info("Subscribing to queues")
    jedis.subscribe(channelListener, COURSE_RATED_EVENT)
  }

  lifecycle.addStopHook { () =>
    Future.successful(())
  }
}
