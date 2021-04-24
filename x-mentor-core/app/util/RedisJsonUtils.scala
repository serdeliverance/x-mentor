package util

import global.EitherResult
import io.redisearch.Document
import models.Course
import models.errors.EmptyResponse
import io.circe.parser.decode
import play.api.Logging

trait RedisJsonUtils {

  self: Logging =>

  def formatJson(string: String): String = string.replaceAll("^\"'|'\"$|\\\\", "")

  def decodeDocument(doc: Document): EitherResult[Course] =
    decode[Course](formatJson(doc.getString("$")))
      .fold(
        error => {
          logger.info(s"Error decoding document: $doc. Error: $error")
          Left(EmptyResponse)
        },
        course => Right(course)
      )
}
