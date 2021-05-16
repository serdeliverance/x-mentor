package util

import controllers.actions.UserRequest
import net.logstash.logback.marker.{LogstashMarker, Markers}
import play.api.MarkerContext
import play.api.mvc.Request

import scala.collection.mutable.{Map => MMap}
import scala.jdk.CollectionConverters._

class MapMarkerContext(val map: MMap[String, String]) extends MarkerContext {
  def marker: Option[LogstashMarker] = Some(Markers.appendEntries(map.asJava))
}

object MapMarkerContext {

  val USERNAME = "username"

  def apply(): MapMarkerContext = new MapMarkerContext(MMap.empty)

  def fromAuthenticatedRequest()(implicit request: UserRequest[_]): MapMarkerContext =
    new MapMarkerContext(MMap(USERNAME -> request.student))

  def fromRequest(map: MMap[String, String] = MMap.empty)(implicit request: Request[_]): MapMarkerContext =
    new MapMarkerContext(map)

}
