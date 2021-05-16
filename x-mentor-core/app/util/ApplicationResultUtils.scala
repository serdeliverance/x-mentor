package util

import global.ApplicationResult
import models.CourseNode

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

  def difference[T](aSet: Seq[T], another: Seq[T]): ApplicationResult[Seq[T]] =
    ApplicationResult {
      aSet.diff(another)
    }
}
