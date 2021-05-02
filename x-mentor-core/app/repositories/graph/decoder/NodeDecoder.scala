package repositories.graph.decoder

import com.redislabs.redisgraph.graph_entities.Node

trait NodeDecoder[T] {
  def decode(node: Node): T
}
