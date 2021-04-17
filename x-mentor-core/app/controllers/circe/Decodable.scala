package controllers.circe

import cats.data.NonEmptyList
import controllers.constants._
import io.circe._
import models.validations.ValidationError
import play.api.Logging
import play.api.libs.circe.Circe
import play.api.mvc.{BaseController, BodyParser, Result}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
 * Custom body parser for validations.
 */
trait Decodable extends Circe with Validatable with Logging { _: BaseController =>

  /**
   * Body parser for json applying validations.
   *
   * @param ec An implicit execution context.
   * @tparam T A circe decoder.
   * @return A body parser.
   */
  implicit protected def decode[T: Decoder](implicit ec: ExecutionContext): BodyParser[T] =
    circe.json.validate(decodeJsonValue[T])

  private def decodeJsonValue[T: Decoder](json: Json): Either[Result, T] =
    implicitly[Decoder[T]]
      .decodeAccumulating(json.hcursor)
      .leftMap { ex =>
        val json = validationErrorsAsInvalidRequestJson(decodingFailuresAsValidationError(ex))
        logger.error(s"Error decoding request. Invalid body:\n$json")
        BadRequest(json)
      }
      .toEither

  private def decodingFailuresAsValidationError(
                                                 decodingFailures: NonEmptyList[DecodingFailure]
                                               ): List[ValidationError] =
    decodingFailures.filterNot(_.message.contains("[A]Option[A]")) map { failure: DecodingFailure =>
      val possiblePath              = CursorOp.opsToPath(failure.history)
      val path: String              = if (failure.history.isEmpty) possiblePath else possiblePath.substring(1)
      val requiredFieldErrorMessage = "required:"
      failure.message match {
        case "Attempt to decode value on failed cursor" =>
          ValidationError(PARAM_REQUIRED, path)

        case message if message.startsWith(requiredFieldErrorMessage) =>
          val field = message.drop(requiredFieldErrorMessage.length)
          ValidationError(PARAM_REQUIRED, field.toString)

        case message =>
          val fieldErrorMessage = "field:"
          val field =
            if (message.startsWith(fieldErrorMessage)) message.drop(fieldErrorMessage.length)
            else path

          ValidationError(INVALID_PARAM, field)
      }
    } distinct

}
