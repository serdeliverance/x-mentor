package repositories.graph

import com.redislabs.redisgraph.ResultSet
import com.redislabs.redisgraph.graph_entities.Node
import repositories.graph.NodeDecoderSyntax.NodeDecoderOps

import scala.collection.mutable.{ListBuffer => MList}

trait ResultDecoder {

  def decode[T](result: ResultSet, entityTag: GraphEntityTag)(implicit nodeDecoder: NodeDecoder[T]): List[T] = {
    var list = MList.empty[T]
    while (result.hasNext) {
      val node: Node = result.next().getValue(entityTag.tag)
      list += node.toDomain
    }
    list.toList
  }
}
