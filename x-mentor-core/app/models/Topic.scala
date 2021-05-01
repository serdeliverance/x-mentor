package models

import com.redislabs.redisgraph.graph_entities.Node
import io.circe.Encoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import models.json.CirceImplicits
import repositories.graph.decoder.NodeDecoder

// TODO analyze if id field is really needed
case class Topic(id: Option[Long], name: String, description: String)

object Topic extends CirceImplicits {

  implicit val topicEncoder: Encoder[Topic] = deriveConfiguredEncoder

  implicit val topicNodeDecoder: NodeDecoder[Topic] =
    new NodeDecoder[Topic] {
      override def decode(node: Node): Topic =
        Topic(
          id = None,
          name = node.getProperty("name").getValue.toString,
          description = node.getProperty("description").getValue.toString
        )
    }
}
