package jobs

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import configurations.MESSAGING_DISPATCHER
import jobs.loaders.DataLoaderManager
import play.api.inject.ApplicationLifecycle
import javax.inject.{Inject, Named, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationStart @Inject()(
    lifecycle: ApplicationLifecycle,
    dataLoaderManager: DataLoaderManager
  )(implicit @Named(MESSAGING_DISPATCHER) ec: ExecutionContext, materializer: Materializer, system: ActorSystem) {

  dataLoaderManager.load()

  CoordinatedShutdown(system).addJvmShutdownHook {
    println("custom JVM shutdown hook...")
    Http().shutdownAllConnectionPools()
    materializer.shutdown()
    system.terminate()
  }

  lifecycle.addStopHook { () =>
    Future.successful(())
  }
}
