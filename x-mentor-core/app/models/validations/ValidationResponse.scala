package models.validations

import models.errors.{ApplicationError, InvalidParameterError}

/**
 * Error representation to use in responses.
 *
 * @param errorType        Error type.
 * @param validationErrors Property with the error.
 */
case class ValidationResponse(errorType: ValidationResponseType, validationErrors: List[InvalidParameterError])
  extends ApplicationError

/**
 * Validation Object
 */
object ValidationResponse {

  /**
   * Create a Validation object from a single validation error
   * @param errorType Validation error type
   * @param validationError validation error
   * @return a Validation object
   */
  def apply(errorType: ValidationResponseType, validationError: InvalidParameterError): ValidationResponse =
    ValidationResponse(errorType, List(validationError))

}

