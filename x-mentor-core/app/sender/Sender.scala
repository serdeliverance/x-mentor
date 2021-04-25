package sender

import global.ApplicationResult

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.ws.{BodyWritable, WSClient, WSResponse}
import util.MapMarkerContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Sender @Inject()(ws: WSClient)(implicit executionContext: ExecutionContext) extends Logging {

  private def wsRequest(url: String, queryParams: List[(String, String)], headers: List[(String, String)]) =
    ws.url(url).withQueryStringParameters(queryParams: _*).withHttpHeaders(headers: _*)

  def get(
      url: String,
      queryParams: List[(String, String)] = Nil,
      headers: List[(String, String)] = Nil
    )(implicit mc: MapMarkerContext
    ): ApplicationResult[WSResponse] =
    toApplicationResult(wsRequest(url, queryParams, headers).get())

  def post[T: BodyWritable](
      url: String,
      body: T,
      headers: List[(String, String)] = List()
    )(implicit mc: MapMarkerContext
    ): ApplicationResult[WSResponse] =
    toApplicationResult(wsRequest(url, Nil, headers).post(body))

  def put[T: BodyWritable](
      url: String,
      body: T,
      headers: List[(String, String)] = List()
    )(implicit mc: MapMarkerContext
    ): ApplicationResult[WSResponse] =
    toApplicationResult(wsRequest(url, Nil, headers).put(body))

  def delete(
      url: String,
      headers: List[(String, String)]
    )(implicit mc: MapMarkerContext
    ): ApplicationResult[WSResponse] = toApplicationResult(wsRequest(url, Nil, headers).delete())

  private def toApplicationResult(
      wsResponse: Future[WSResponse]
    )(implicit mc: MapMarkerContext
    ): ApplicationResult[WSResponse] =
    wsResponse
      .map(Right(_))
      .recover {
        case e =>
          logger.error(s"Connection error", e)
          Left(UnexpectedExecutionError(e))
      }
}
