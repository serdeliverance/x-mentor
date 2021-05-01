package util

import global.ApplicationResult

import scala.util.Random

trait RandomUtils {

  def randomInt(maxValue: Int): Int = {
    val min    = 0
    val random = new Random()
    min + random.nextInt((maxValue - min) + 1)
  }

  def takeRandomFromList[T](list: List[T]): ApplicationResult[T] =
    ApplicationResult(list(randomInt(list.length - 1)))
}
