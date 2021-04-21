package repositories

import akka.Done
import akka.Done.done
import global.ApplicationResult
import io.redisearch.client.Client.IndexOptions
import io.redisearch.{Query, Schema}
import io.redisearch.client.Client
import javax.inject.{Inject, Singleton}
import models.errors.UnexpectedError
import play.api.Logging
import util.JsonParsingUtils
import scala.jdk.CollectionConverters._
import io.redisearch.Document
import scala.util.Try

@Singleton
class RediSearchRepository @Inject()(rediSearch: Client) extends Logging with JsonParsingUtils {

  def search(query: Query): ApplicationResult[(Long, List[Document])] = {
    Try(rediSearch.search(query))
      .fold(
        error => {
          logger.info(s"Error searching with query: ${query.toString}")
          ApplicationResult.error(UnexpectedError(error))
        },
        searchResult => {
          ApplicationResult((searchResult.totalResults, searchResult.docs.asScala.toList))
        }
      )
  }

  def createIndex(schema: Schema, options: IndexOptions): ApplicationResult[Done] = {
    logger.info(s"Creating index $schema")
    Try(rediSearch.createIndex(schema, options))
      .fold(
        error => {
          logger.info(s"Error creating index.")
          ApplicationResult.error(UnexpectedError(error))
        },
        _ => ApplicationResult(done())
      )
  }

}
