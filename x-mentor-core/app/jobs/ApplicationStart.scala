package jobs

import configurations.MESSAGING_DISPATCHER
import jobs.loaders.DataLoaderManager
import play.api.Logging
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationStart @Inject()(
    lifecycle: ApplicationLifecycle,
    dataLoaderManager: DataLoaderManager
  )(implicit @Named(MESSAGING_DISPATCHER) ec: ExecutionContext)
    extends Logging {

  dataLoaderManager.load()

  lifecycle.addStopHook { () =>
    Future.successful(())
  }
}
