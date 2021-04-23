package jobs.loaders

import akka.Done
import play.api.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataLoaderManager @Inject()(
    topicLoader: TopicLoader,
    courseLoader: CourseLoader,
    studentLoader: StudentLoader,
    filterLoader: FilterLoader,
    indexLoader: IndexLoader,
    hasRelationLoader: HasRelationLoader,
)(implicit executionContext: ExecutionContext)
    extends Logging {

  def load(): Future[Done] = {
    logger.info("Loading all data into the graph")

    for {
      _ <- topicLoader.loadTopics()
      _ <- courseLoader.loadCoursesToGraph()
      _ <- courseLoader.loadCourses()
      _ <- filterLoader.loadFilters()
      // _ <- indexLoader.loadIndexes()
      _ <- hasRelationLoader.loadHasRelations()
      _ <- studentLoader.loadStudents()
      // _ <- loadInterestRelations()
      // _ <- loadStudyingRelations()
      // _ <- loadRateRelations()
      // _ <- loadTeachers()
    } yield Done
  }
}
