package sender

import models.errors.ApplicationError
import play.api.libs.ws.WSResponse

case class HttpResponseError(WSResponse: WSResponse) extends ApplicationError

case class UnexpectedExecutionError(error: Throwable) extends ApplicationError
