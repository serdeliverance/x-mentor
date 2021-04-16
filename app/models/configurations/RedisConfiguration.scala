package models.configurations

import scala.concurrent.duration.FiniteDuration

case class RedisConfiguration(
    host: String,
    port: Int,
    password: Option[String],
    database: Int,
    poolSize: Int,
    connectionTimeout: FiniteDuration,
    executionTimeout: FiniteDuration)
