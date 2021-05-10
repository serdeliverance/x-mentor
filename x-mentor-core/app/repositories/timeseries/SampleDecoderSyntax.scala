package repositories.timeseries

import com.redislabs.redistimeseries.Value

import scala.util.Try

object SampleDecoderSyntax {

  def decodeSample[T](key: String, value: Array[Value])(implicit decoder: SampleDecoder[T]): Try[T] =
    decoder.decodeSample(key, value)
}
