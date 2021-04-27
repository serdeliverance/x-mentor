package util

import net.logstash.logback.marker.{LogstashMarker, Markers}
import play.api.MarkerContext
import play.api.mvc.Request

import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.collection.mutable.{Map => MMap}

class MapMarkerContext(val map: MMap[String, String], val uow: String) extends MarkerContext {

  map ++= MMap(MapMarkerContext.UOW -> uow)

  def marker: Option[LogstashMarker] = Some(Markers.appendEntries(map.asJava))
}

object MapMarkerContext {

  val UOW      = "uow"
  val USERNAME = "username"
  val EMAIL    = "email"

  private def newUow: String = UUID.randomUUID().toString

  def apply(): MapMarkerContext = new MapMarkerContext(MMap.empty, newUow)

  def fromRequest(map: MMap[String, String] = MMap.empty)(implicit request: Request[_]): MapMarkerContext =
    new MapMarkerContext(map, request.headers.get(UOW).getOrElse(newUow))

}
