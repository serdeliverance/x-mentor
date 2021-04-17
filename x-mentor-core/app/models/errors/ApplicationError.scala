package models.errors

import models.validations.ValidationErrorCode

trait ApplicationError

case class InvalidParameterError(code: ValidationErrorCode, parameter: String)

case object EmptyResponse extends ApplicationError

case class DataBaseError(msg: String) extends ApplicationError

case class UnexpectedError(throwable: Throwable) extends ApplicationError

case class ClientError(errorMessage: String) extends ApplicationError

case object ExternalServiceError extends ApplicationError

case class GenericError(msg: String) extends ApplicationError
