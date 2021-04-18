package models

import com.redislabs.redisgraph.graph_entities.Node
import io.circe.Encoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import models.json.CirceImplicits
import repositories.graph.NodeDecoder

/**
  * Represents a course in the context of graph. This difference was created to avoid conflicts with Course model,
  * which contains info related to course content and which is used on a json related/frontend focused context.
  */
case class CourseNode(name: String)

object CourseNode extends CirceImplicits {
  implicit val courseNodeNodeDecoder: NodeDecoder[CourseNode] = new NodeDecoder[CourseNode] {
    override def decode(node: Node): CourseNode =
      CourseNode(node.getProperty("name").getValue.toString)
  }

  implicit val courseNodeEncoder: Encoder[CourseNode] = deriveConfiguredEncoder
}
