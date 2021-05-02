package repositories

import akka.Done
import akka.Done.done
import global.ApplicationResult
import io.redisearch.client.Client
import io.redisearch.client.Client.IndexOptions
import io.redisearch.{Document, Query, Schema}
import models.errors.UnexpectedError
import play.api.Logging
import util.JsonUtils
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class RediSearchRepository @Inject()(rediSearch: Client)(implicit ec: ExecutionContext) extends Logging with JsonUtils {

  def search(query: Query): ApplicationResult[(Long, List[Document])] =
    Try(rediSearch.search(query))
      .fold(
        error => {
          logger.info(s"Error searching courses ")
          ApplicationResult.error(UnexpectedError(error))
        },
        searchResult => {
          ApplicationResult((searchResult.totalResults, searchResult.docs.asScala.toList))
        }
      )

  def get(course: String): ApplicationResult[Option[Document]] = {
    logger.info(s"Retrieving course: $course from redis json")
    val query = new Query(course)
    search(query)
      .map {
        case Right(searchResult) => Right(searchResult._2.headOption)
        case Left(error) =>
          logger.info(s"Error getting course: $course from redis json")
          Left(error)
      }
  }

  def createIndex(schema: Schema, options: IndexOptions): ApplicationResult[Done] = {
    logger.info(s"Creating index $schema")
    Try(rediSearch.createIndex(schema, options))
      .fold(
        error => {
          logger.info(s"Error creating index. Error: $error")
          ApplicationResult.error(UnexpectedError(error))
        },
        _ => ApplicationResult(done())
      )
  }

}
