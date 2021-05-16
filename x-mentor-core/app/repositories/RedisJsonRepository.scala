package repositories

import akka.Done
import akka.Done.done
import com.google.gson.Gson
import com.redislabs.modules.rejson.JReJSON
import global.ApplicationResult
import io.circe.Decoder
import models.errors.{ClientError, UnexpectedError}
import play.api.Logging
import javax.inject.{Inject, Singleton}

import scala.util.Try
import io.circe.parser.decode
import util.JsonUtils

@Singleton
class RedisJsonRepository @Inject()(redisJson: JReJSON) extends Logging with JsonUtils {

  val gson = new Gson()

  def get[T](key: String)(implicit decoder: Decoder[T]): ApplicationResult[T] =
    Try(redisJson.get[T](key))
      .fold(
        error => {
          logger.error(s"Error getting key: $key from redisJSON. Error: $error")
          ApplicationResult.error(UnexpectedError(error))
        },
        jsonString => {
          decode[T](gson.toJson(jsonString))
            .fold(
              _ => {
                logger.error(s"Error decoding $jsonString")
                ApplicationResult.error(ClientError("Error decoding redisJSON response"))
              },
              value => ApplicationResult(value)
            )
        }
      )

  def set(key: String, jsonString: Object): ApplicationResult[Done] = {
    logger.info(s"Uploading json with key: $key to redisJson")
    Try(redisJson.set(key, jsonString))
      .fold(
        error => {
          logger.info(s"Error uploading json to redisJson. Key: $key")
          ApplicationResult.error(UnexpectedError(error))
        },
        _ => ApplicationResult(done())
      )
  }
}
