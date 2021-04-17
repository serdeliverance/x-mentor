package controllers.circe

import controllers.constants._
import io.circe.Json
import io.circe.syntax._
import models.json.CirceImplicits
import models.validations.{ValidationError, ValidationObject}

/**
 * Trait that contains helper methods to create Validation errors.
 */
trait Validatable extends CirceImplicits {

  /**
   * Create an Invalid request and converts it to Json in order to handle Validation errors.
   *
   * @param validationErrors List of errors.
   * @param errorType Type of error.
   * @return Json representation of the list of errors.
   */
  protected def validationErrorsAsInvalidRequestJson(
                                                      validationErrors: List[ValidationError],
                                                      errorType: String = INVALID_REQUEST_ERROR
                                                    ): Json = validationErrorsAsInvalidRequest(validationErrors, errorType).asJson

  /**
   * Create an Invalid request in order to provide the Validation errors.
   *
   * @param validationErrors List of errors.
   * @param errorType Type of error.
   * @return A [[models.validations.ValidationObject]] being the content of the invalid request.
   */
  protected def validationErrorsAsInvalidRequest(
                                                  validationErrors: List[ValidationError],
                                                  errorType: String = INVALID_REQUEST_ERROR
                                                ): ValidationObject = ValidationObject(errorType = errorType, validationErrors = validationErrors)

}
