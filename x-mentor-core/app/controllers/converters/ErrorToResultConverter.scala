package controllers.converters

import models.errors.{ApplicationError, ClientError, DataBaseError}
import play.api.mvc.Results.InternalServerError

trait ErrorToResultConverter {

  def handleError(error: ApplicationError) = error match {
    case ClientError(_)   => InternalServerError
    case DataBaseError(_) => InternalServerError
    case _                => InternalServerError
  }
}
