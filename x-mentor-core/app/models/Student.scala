package models

import com.redislabs.redisgraph.graph_entities.Node
import repositories.graph.decoder.NodeDecoder

case class Student(username: String, email: String)

object Student {
  implicit val studentNodeDecoder: NodeDecoder[Student] =
    (node: Node) => Student(
      username = node.getProperty("username").getValue.toString,
      email = node.getProperty("email").getValue.toString
    )
}
