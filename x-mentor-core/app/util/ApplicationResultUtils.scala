package util

import global.ApplicationResult

import scala.concurrent.{ExecutionContext, Future}

trait ApplicationResultUtils {

  def sequence[T](results: Seq[ApplicationResult[T]])(implicit ec: ExecutionContext): ApplicationResult[Seq[T]] =
    Future
      .sequence(results)
      .map(
        _.collect {
          case Right(t) => t
        }
      )
      .map(Right(_))
}
