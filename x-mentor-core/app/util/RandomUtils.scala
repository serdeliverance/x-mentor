package util

import scala.util.Random

trait RandomUtils {

  def randomInt(maxValue: Int): Int = {
    val min    = 0
    val random = new Random()
    min + random.nextInt((maxValue - min) + 1)
  }
}
