package global

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Provides}
import com.redislabs.modules.rejson.JReJSON
import com.redislabs.redisgraph.impl.api.RedisGraph
import jobs.ApplicationStart
import configurations._
import io.rebloom.client.Client
import play.api.libs.concurrent.{AkkaGuiceSupport, CustomExecutionContext}
import play.api.{Configuration, Environment}
import queues.MessageHandler.CourseRated
import queues.{ChannelListener, MessageHandler}
import redis.clients.jedis.util.Pool
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}
import javax.inject.{Named, Singleton}
import models.configurations.{RedisConfiguration, RedisGraphConfiguration}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {

  val _ = environment

  override def configure(): Unit = {
    bindActor[MessageHandler](MESSAGE_HANDLER_ACTOR)
    bind(classOf[ApplicationStart]).asEagerSingleton()
  }

  @Provides
  def redisConfiguration: RedisConfiguration = RedisConfiguration(
    host = configuration.get[String](REDIS_HOST),
    port = configuration.get[Int](REDIS_PORT),
    password = configuration.getOptional[String](REDIS_PASSWORD),
    database = configuration.get[Int](REDIS_DATABASE),
    poolSize = configuration.get[Int](REDIS_POOL_SIZE),
    connectionTimeout = configuration.get[Long](REDIS_CONNECTION_TIMEOUT_MILLIS).millis,
    executionTimeout = configuration.get[Long](REDIS_EXECUTION_TIMEOUT_MILLIS).millis
  )

  @Provides
  def redisPool(config: RedisConfiguration): Pool[Jedis] = {
    val poolConfig = new JedisPoolConfig()

    poolConfig.setMaxWaitMillis(config.executionTimeout.toMillis)
    poolConfig.setMaxTotal(config.poolSize)

    new JedisPool(
      poolConfig,
      config.host,
      config.port,
      config.connectionTimeout.toMillis.toInt,
      config.password.orNull,
      config.database
    )
  }

  @Provides
  def redisGraph(): RedisGraph =
    new RedisGraph()

  @Provides
  def redisGraphConfiguration(): RedisGraphConfiguration =
    RedisGraphConfiguration(configuration.get[String](REDIS_GRAPH))

  @Provides @Singleton @Named(COURSE_RATED_TOPIC)
  def courseRatedTopicSubscription(
      @Named(MESSAGE_HANDLER_ACTOR) messageConsumer: ActorRef
    ): ChannelListener[CourseRated] =
    new ChannelListener[CourseRated](messageConsumer)

  @Provides @Singleton @Named(COURSE_RECOMMENDED_TOPIC)
  def topicRatedTopicSubscription(
      @Named(MESSAGE_HANDLER_ACTOR) messageConsumer: ActorRef
    ): ChannelListener[CourseRated] =
    new ChannelListener[CourseRated](messageConsumer)

  @Provides @Singleton @Named(MESSAGING_DISPATCHER)
  def messagingExecutionContext(system: ActorSystem): ExecutionContext =
    new CustomExecutionContext(system, MESSAGING_DISPATCHER) {}

  @Provides
  def redisBlooms(config: RedisConfiguration): Client = new Client(redisPool(config))

  @Provides
  def redisJSON(config: RedisConfiguration): JReJSON = new JReJSON(redisPool(config))

  @Provides
  def rediSearch(config: RedisConfiguration): io.redisearch.client.Client = new io.redisearch.client.Client("xmentor", redisPool(config))
}
