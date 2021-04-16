package models.validations

import java.security.InvalidParameterException

/**
 * Validation Response Type
 */
sealed trait ValidationResponseType

/**
 * Invalid request error type
 */
case object InvalidRequestError extends ValidationResponseType

/**
 * Not Found error type
 */
case object NotFoundError extends ValidationResponseType

/**
 * Validation response type companion object
 */
object ValidationResponseType {

  /**
   * Apply method
   * @param validationErrorType string value
   * @return a Validation error type
   */
  def apply(validationErrorType: String): ValidationResponseType = validationErrorType match {
    case value if value equals InvalidRequestError.toString => InvalidRequestError
    case value if value equals NotFoundError.toString       => NotFoundError
    case value =>
      throw new InvalidParameterException(s"$value did not match any ValidationErrorType.")
  }
}
