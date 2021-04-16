import models.errors.ApplicationError
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

package object global {

  type ApplicationResult[+T] = Future[EitherResult[T]]
  type EitherResult[+T]      = Either[ApplicationError, T]

  object ApplicationResult {
    def apply[T](t: T): ApplicationResult[T]                                    = Future.successful(Right(t))
    def error(error: ApplicationError): Future[Left[ApplicationError, Nothing]] = Future.successful(Left(error))
  }

  implicit class ApplicationResultExtended[A](applicationResult: ApplicationResult[A]) {

    def innerFlatMap[B](f: A => ApplicationResult[B])(implicit ec: ExecutionContext): ApplicationResult[B] =
      applicationResult.flatMap(_.fold(ApplicationResult.error, f))

    def innerMap[B](f: A => EitherResult[B])(implicit ec: ExecutionContext): ApplicationResult[B] =
      applicationResult.map(_.fold(Left(_), v => f(v)))

    def innerHandleError(f: ApplicationError => A)(implicit ec: ExecutionContext): ApplicationResult[A] =
      applicationResult map {
        case Left(err)    => Right(f(err))
        case Right(value) => Right(value)
      }

    def mapEitherResult(
        handleSuccess: A => Result,
        handleError: ApplicationError => Result
      )(implicit ec: ExecutionContext
      ): Future[Result] =
      applicationResult.map(_.fold(handleError, handleSuccess))
  }
}
