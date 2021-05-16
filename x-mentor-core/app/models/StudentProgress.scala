package models

import com.redislabs.redistimeseries.Value
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import models.json.CirceImplicits
import repositories.timeseries.SampleDecoder

import scala.util.Try

case class StudentProgress(student: String, progress: Int)

object StudentProgress extends CirceImplicits {

  implicit val studentProgressEncoder: Encoder[StudentProgress] = deriveConfiguredEncoder
  implicit val studentProgressDecoder: Decoder[StudentProgress] = deriveConfiguredDecoder

  implicit val studentProgressSampleDecoder: SampleDecoder[StudentProgress] = new SampleDecoder[StudentProgress] {
    override def decodeSample(key: String, value: Array[Value]): Try[StudentProgress] =
      for {
        student  <- Try(key.split(":")(1))
        progress <- Try(value(0).getValue.toInt)
      } yield StudentProgress(student, progress)
  }
}
