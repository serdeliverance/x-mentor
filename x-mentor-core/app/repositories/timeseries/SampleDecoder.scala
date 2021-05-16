package repositories.timeseries

import com.redislabs.redistimeseries.Value

import scala.util.Try

trait SampleDecoder[T] {
  def decodeSample(key: String, value: Array[Value]): Try[T]
}
