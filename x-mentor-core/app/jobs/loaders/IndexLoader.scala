package jobs.loaders

import java.nio.file.Paths

import akka.Done.done
import akka.actor.ActorSystem
import akka.Done

import javax.inject.{Inject, Singleton}
import play.api.Logging
import repositories.RediSearchRepository

import scala.concurrent._

@Singleton
class IndexLoader @Inject()(
    rediSearchRepository: RediSearchRepository
  )(implicit system: ActorSystem,
    ec: ExecutionContext)
    extends Logging {

  def loadIndexes(): Future[Done] =
    Future(done())

}
