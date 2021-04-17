package util

trait JsonParsingUtils {
  def formatJsonResponse(response: String) = response.substring(1, response.length - 1)
}
