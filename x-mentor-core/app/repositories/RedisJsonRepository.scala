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
import com.redislabs.modules.rejson.Path

import scala.util.Try
import io.circe.parser.decode
import util.JsonParsingUtils

@Singleton
class RedisJsonRepository @Inject()(redisJson: JReJSON) extends Logging with JsonParsingUtils {

  def get[T](key: String)(implicit decoder: Decoder[T]): ApplicationResult[T] =
    Try(redisJson.get[String](key))
      .fold(
        error => {
          logger.info(s"Error getting key: $key from redisJSON")
          ApplicationResult.error(UnexpectedError(error))
        },
        jsonString => {
          decode[T](formatJsonResponse(jsonString))
            .fold(
              _ => {
                logger.info(s"Error decoding $jsonString")
                ApplicationResult.error(ClientError("Error decoding redisJSON response"))
              },
              value => ApplicationResult(value)
            )
        }
      )

  def set(key: String, jsonString: Object): ApplicationResult[Done] = {
    val gson: Gson = new Gson

    logger.info(s"Uploading json with key: $key to redisJson")
    Try(redisJson.set(key, jsonString, new Path("$")))
      .fold(
        error => {
          logger.info(s"Error uploading json to redisJson. Key: $key")
          ApplicationResult.error(UnexpectedError(error))
        },
        _ => ApplicationResult(done())
      )
  }

  def formatJson(string: String): String = string.replaceAll("^\"'|'\"$|\\\\", "")

}
