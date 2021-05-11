package jobs

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import configurations.MESSAGING_DISPATCHER
import jobs.loaders.DataLoaderManager
import play.api.inject.ApplicationLifecycle
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class ApplicationStart @Inject()(
    lifecycle: ApplicationLifecycle,
    dataLoaderManager: DataLoaderManager
  )(implicit @Named(MESSAGING_DISPATCHER) ec: ExecutionContext, system: ActorSystem) {

  dataLoaderManager.load()

  lifecycle.addStopHook { () =>
    Future {
      Await.result(Http().shutdownAllConnectionPools(), 10 seconds)
      Await.result(system.terminate(), 10 seconds)
    }
  }
}
