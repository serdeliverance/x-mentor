package util

trait RedisStreamsUtils {

  def format[T](cc: T): Map[String, String] =
    cc.getClass.getDeclaredFields.foldLeft(Map.empty[String, String]) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc).toString)
    }
}
