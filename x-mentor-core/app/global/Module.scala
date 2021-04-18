package global

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import com.redislabs.modules.rejson.JReJSON
import com.redislabs.redisgraph.impl.api.RedisGraph
import configurations._
import io.rebloom.client.Client
import jobs.ApplicationStart
import models.configurations.{RedisConfiguration, RedisGraphConfiguration}
import play.api.libs.concurrent.{AkkaGuiceSupport, CustomExecutionContext}
import play.api.{Configuration, Environment}
import redis.clients.jedis.util.Pool
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import javax.inject.{Named, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {

  val _ = environment

  override def configure(): Unit =
    bind(classOf[ApplicationStart]).asEagerSingleton()

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
  def redisGraph(config: RedisConfiguration): RedisGraph =
    new RedisGraph(redisPool(config))

  @Provides
  def redisGraphConfiguration(): RedisGraphConfiguration =
    RedisGraphConfiguration(configuration.get[String](REDIS_GRAPH))

  @Provides @Singleton @Named(MESSAGING_DISPATCHER)
  def messagingExecutionContext(system: ActorSystem): ExecutionContext =
    new CustomExecutionContext(system, MESSAGING_DISPATCHER) {}

  @Provides
  def redisBlooms(config: RedisConfiguration): Client = new Client(redisPool(config))

  @Provides
  def redisJSON(config: RedisConfiguration): JReJSON = new JReJSON(redisPool(config))

  @Provides
  def rediSearch(config: RedisConfiguration): io.redisearch.client.Client =
    new io.redisearch.client.Client("xmentor", redisPool(config))
}
