package global

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import com.redislabs.modules.rejson.JReJSON
import com.redislabs.redisgraph.impl.api.RedisGraph
import com.redislabs.redistimeseries.RedisTimeSeries
import configurations._
import io.rebloom.client.Client
import jobs.ApplicationStart
import models.configurations._
import play.api.libs.concurrent.{AkkaGuiceSupport, CustomExecutionContext}
import play.api.{Configuration, Environment}
import redis.clients.jedis.util.Pool
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

import javax.inject.{Named, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong
import constants.COURSE_IDX

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
    poolConfig.setMaxIdle(config.poolSize)

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
    new io.redisearch.client.Client(COURSE_IDX, redisPool(config))

  @Provides
  def authConfiguration: AuthConfiguration = new AuthConfiguration(
    urls = new AuthUrlsConfiguration(
      base = this.configuration.get[String](AUTH_BASE_URL),
      token = this.configuration.get[String](AUTH_TOKEN_URL),
      users = this.configuration.get[String](AUTH_USERS_URL),
      realm = this.configuration.get[String](AUTH_REALM_URL),
      logout = this.configuration.get[String](AUTH_LOGOUT_URL)
    ),
    users = new UsersConfiguration(
      admin = new UserConfiguration(
        username = this.configuration.get[String](AUTH_ADMIN_USERNAME),
        password = this.configuration.get[String](AUTH_ADMIN_PASSWORD)
      ),
      usernameAttributeName = this.configuration.get[String](USERNAME_ATTRIBUTE_NAME)
    ),
    clientId = this.configuration.get[String](AUTH_CLIENT_ID),
    clientSecret = this.configuration.get[String](AUTH_CLIENT_SECRET),
    grantType = this.configuration.get[String](AUTH_GRANT_TYPE),
    scope = this.configuration.get[String](AUTH_SCOPE)
  )

  @Provides
  def recommendationConfig(): RecommendationConfig = RecommendationConfig(
    enrolledRecommendationSize = configuration.get[Int](ENROLLED_RECOMMENDATION_SIZE),
    interestRecommendationSize = configuration.get[Int](INTEREST_RECOMMENDATION_SIZE),
    discoveryRecommendationSize = configuration.get[Int](DISCOVER_RECOMMENDATION_SIZE)
  )

  @Provides
  def redisTimeSeries(pool: Pool[Jedis]): RedisTimeSeries =
    new RedisTimeSeries(pool)
}
