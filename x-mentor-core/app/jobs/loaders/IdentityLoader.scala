package jobs.loaders

import akka.actor.ActorSystem
import constants.PUBLIC_KEY
import javax.inject.{Inject, Singleton}
import models.auth.RealmResponse
import models.configurations.AuthConfiguration
import play.api.Logging
import play.api.libs.ws.WSResponse
import sender.Sender
import io.circe.parser.decode
import repositories.RedisRepository
import util.MapMarkerContext

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IdentityLoader @Inject()(sender: Sender, configuration: AuthConfiguration, redisRepository: RedisRepository)(implicit system: ActorSystem, ec: ExecutionContext) extends Logging {

  def loadPublicKey(): Future[Boolean] = {
    implicit val markerContext: MapMarkerContext = MapMarkerContext.apply()
    Future {
      logger.info("Getting public key")
      sender.get(this.configuration.urls.realmUrl)
    } match {
      case response: WSResponse => response.status match {
        case 200 =>
          logger.info(s"Success login")
          decode[RealmResponse](response.body)
            .fold(
              error => {
                logger.error(s"Error parsing auth server response $error")
                Future(false)
              },
              realmResponse => redisRepository.set(PUBLIC_KEY, realmResponse.publicKey)
            )
        case _ =>
          logger.info("Failing connecting with auth server")
          Future(false)
      }
    }
  }
}
