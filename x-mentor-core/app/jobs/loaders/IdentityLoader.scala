package jobs.loaders

import akka.actor.ActorSystem
import cats.data.EitherT
import constants.PUBLIC_KEY
import global.ApplicationResult
import javax.inject.{Inject, Singleton}
import models.auth.RealmResponse
import models.configurations.AuthConfiguration
import play.api.Logging
import play.api.libs.ws.WSResponse
import sender.Sender
import io.circe.parser.decode
import repositories.RedisRepository
import util.MapMarkerContext
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IdentityLoader @Inject()(sender: Sender, configuration: AuthConfiguration, redisRepository: RedisRepository)(implicit system: ActorSystem, ec: ExecutionContext) extends Logging {

  def loadPublicKey(): Future[Boolean] = {
    implicit val markerContext: MapMarkerContext = MapMarkerContext.apply()
    logger.info("Getting public key")

    for {
      response <- EitherT(sender.get(this.configuration.urls.realmUrl))
      result <- EitherT(handleResponse(response))
    } yield result
  }.value.map(_.getOrElse(false))

    def handleResponse(response: WSResponse): ApplicationResult[Boolean] =
      response.status match {
        case 200 =>
          logger.info(s"Public key retrieved")
          decode[RealmResponse](response.body)
            .fold(
              error => {
                logger.error(s"Error parsing auth server response $error")
                ApplicationResult(false)
              },
              realmResponse => redisRepository.set(PUBLIC_KEY, realmResponse.publicKey).map(result => Right(result))
            )
        case _ =>
          logger.error("Failing connecting with auth server")
          ApplicationResult(false)
      }
}
