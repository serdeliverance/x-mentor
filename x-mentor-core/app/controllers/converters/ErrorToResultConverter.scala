package controllers.converters

import models.errors.{ApplicationError, ClientError, DataBaseError, InvalidOperationError}
import play.api.mvc.Results
import play.api.mvc.Results.{BadRequest, InternalServerError}

trait ErrorToResultConverter {

  def handleError(error: ApplicationError): Results.Status = error match {
    case ClientError(_)           => InternalServerError
    case DataBaseError(_)         => InternalServerError
    case InvalidOperationError(_) => BadRequest // TODO add error message to response
    case _                        => InternalServerError
  }
}
