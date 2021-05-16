package repositories.graph.decoder

import com.redislabs.redisgraph.graph_entities.Node

object NodeDecoderSyntax {

  implicit class NodeDecoderOps[T](node: Node) {

    def toDomain(implicit nodeDecoder: NodeDecoder[T]): T =
      nodeDecoder.decode(node)
  }
}
