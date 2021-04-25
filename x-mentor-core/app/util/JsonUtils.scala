package util

import io.circe.{Decoder, HCursor, Json}
import io.circe.parser.parse

trait JsonUtils {

  def formatJsonResponse(response: String): String = response.substring(1, response.length - 1)

  /**
    * Generic method that extracts a field at the root level of a json object
    *
    * @param json
    * @param fieldName
    * @param decoder
    * @tparam T
    * @return
    */
  def extractValue[T](json: String, fieldName: String)(implicit decoder: Decoder[T]): Option[T] = {
    val doc: Json       = parse(json).getOrElse(Json.Null)
    val cursor: HCursor = doc.hcursor

    cursor.downField(fieldName).as[T].toOption
  }
}
