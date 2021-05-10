package util

import java.time.Instant
import java.time.temporal.ChronoUnit

trait UnixTimestampUtils {

  def threeMonthsBack(): Long = Instant.now().minus(90, ChronoUnit.DAYS).getEpochSecond
  def now(): Long             = Instant.now().getEpochSecond
}
