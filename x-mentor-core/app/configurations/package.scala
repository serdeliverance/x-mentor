package object configurations {

  final val AUTH_PUBLIC_KEY = "auth.pulicKey"

  final val MESSAGING_DISPATCHER      = "x-mentor.dispatchers.messaging"
  val REDIS_HOST                      = "redis.host"
  val REDIS_PORT                      = "redis.port"
  val REDIS_PASSWORD                  = "redis.password"
  val REDIS_DATABASE                  = "redis.database"
  val REDIS_CONNECTION_TIMEOUT_MILLIS = "redis.connection-timeout-millis"
  val REDIS_EXECUTION_TIMEOUT_MILLIS  = "redis.execution-timeout-millis"
  val REDIS_POOL_SIZE                 = "redis.pool-size"

  val REDIS_GRAPH = "redis.graph"

  // TODO delete those variables if not used
  final val COURSE_RATED_TOPIC       = "course-rated-topic"
  final val COURSE_RECOMMENDED_TOPIC = "course-recommended-topic"
}
