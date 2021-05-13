package object configurations {

  final val MESSAGING_DISPATCHER      = "x-mentor.dispatchers.messaging"
  val REDIS_HOST                      = "redis.host"
  val REDIS_PORT                      = "redis.port"
  val REDIS_PASSWORD                  = "redis.password"
  val REDIS_DATABASE                  = "redis.database"
  val REDIS_CONNECTION_TIMEOUT_MILLIS = "redis.connection-timeout-millis"
  val REDIS_EXECUTION_TIMEOUT_MILLIS  = "redis.execution-timeout-millis"
  val REDIS_POOL_SIZE                 = "redis.pool-size"

  val REDIS_GRAPH = "redis.graph"

  val AUTH_BASE_URL           = "auth.urls.base"
  val AUTH_TOKEN_URL          = "auth.urls.token"
  val AUTH_USERS_URL          = "auth.urls.users"
  val AUTH_REALM_URL          = "auth.urls.realm"
  val AUTH_ADMIN_USERNAME     = "auth.users.admin.username"
  val AUTH_ADMIN_PASSWORD     = "auth.users.admin.password"
  val USERNAME_ATTRIBUTE_NAME = "auth.users.usernameAttributeName"
  val AUTH_CLIENT_ID          = "auth.clientId"
  val AUTH_ADMIN_CLIENT_ID    = "auth.adminClientId"
  val AUTH_CLIENT_SECRET      = "auth.clientSecret"
  val AUTH_SCOPE              = "auth.scope"
  val AUTH_GRANT_TYPE         = "auth.grantType"
  val AUTH_LOGOUT_URL         = "auth.urls.logout"
  val AUTH_PUBLIC_KEY         = "auth.publicKey"

  val ENROLLED_RECOMMENDATION_SIZE = "recommendations.enrolled-recommendation-size"
  val INTEREST_RECOMMENDATION_SIZE = "recommendations.interest-recommendation-size"
  val DISCOVER_RECOMMENDATION_SIZE = "recommendations.discover-recommendation-size"
}
