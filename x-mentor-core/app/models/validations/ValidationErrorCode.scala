package models.validations

import java.security.InvalidParameterException

/**
 * Error Category
 */
sealed trait ValidationErrorCode

/**
 * Invalid Param
 */
case object InvalidParam extends ValidationErrorCode

/**
 * Param Missing
 */
case object ParamRequired extends ValidationErrorCode

/**
 * Validation Error Code companion object
 */
object ValidationErrorCode {

  /**
   * Creates a validation error code from a String value
   * @param validationErrorCode string value
   * @return validation error code
   */
  def apply(validationErrorCode: String): ValidationErrorCode = validationErrorCode match {
    case value if value equals InvalidParam.toString  => InvalidParam
    case value if value equals ParamRequired.toString => ParamRequired
    case value =>
      throw new InvalidParameterException(s"$value did not match any ValidationErrorCode.")
  }
}

