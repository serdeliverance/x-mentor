package services

import akka.Done
import akka.Done.done
import cats.data.EitherT
import global.ApplicationResult
import io.circe.parser.decode
import models.auth.{AccessData, AuthErrorResponse}
import models.configurations.AuthConfiguration
import models.errors.{AuthenticationError, ClientError, EmptyResponse}
import models.json.CirceImplicits
import play.api.Logging
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.WSResponse
import sender.Request.formUrlEncodedBody
import sender.Sender
import util.MapMarkerContext
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext
import cats.implicits._
import play.api.libs.json._

@Singleton
class UserService @Inject()(sender: Sender, configuration: AuthConfiguration)(implicit ec: ExecutionContext)
    extends Logging
    with CirceImplicits {

  def login(
    username: String,
    password: String
  )(implicit mapMarkerContext: MapMarkerContext
  ): ApplicationResult[AccessData] = {
    logger.info(s"login in user: $username")

    val reqBody    = createAuthRequestBody(username, password, this.configuration.clientId)
    val reqHeaders = List((HeaderNames.CONTENT_TYPE, MimeTypes.FORM))

    val result = for {
      authResponse <- EitherT { sender.post(this.configuration.urls.tokenUrl, reqBody, reqHeaders) }
      accessData   <- EitherT { handleAuthResponse(authResponse) }
    } yield accessData

    result.value
  }

  def signup(
    username: String,
    password: String
  )(implicit mapMarkerContext: MapMarkerContext
  ): ApplicationResult[Done] =
    ApplicationResult {
      logger.info(s"Creating user: $username")

      val requestTokenBody    = createAuthRequestBody(username, password, this.configuration.adminClientId)
      val requestTokenHeaders = List((HeaderNames.CONTENT_TYPE, MimeTypes.FORM))

      val createUserBody    = Json.obj(
        "username" -> username,
        "enabled" -> "true",
        "credentials" -> s"[{'type':'password','value':'$username','temporary':false}]"
      )
      val createUserHeaders = List((HeaderNames.CONTENT_TYPE, MimeTypes.JSON))

      logger.info(s"$createUserBody")

      val result = for {
        adminToken   <- EitherT { sender.post(this.configuration.urls.adminTokenUrl, requestTokenBody, requestTokenHeaders) }
        creationResponse <- EitherT { sender.post(this.configuration.urls.usersUrl, createUserBody, createUserHeaders.appended((HeaderNames.AUTHORIZATION, s"Bearer $adminToken"))) }
      } yield creationResponse
      Done
    }

  /**
    * Handles the response from Auth service and matches it with the corresponding [[ApplicationResult]]
    *
    * @param response wsResponse
    * @return
    */
  private def handleAuthResponse(response: WSResponse): ApplicationResult[AccessData] =
    response.status match {
      case 200 =>
        logger.info(s"Success login")
        decode[AccessData](response.body)
          .fold(
            error => {
              logger.error(s"Error parsing auth server response $error")
              ApplicationResult.error(ClientError(s"Error parsing auth server response: $error"))
            },
            accessData => ApplicationResult(accessData)
          )
      case 400 =>
        logger.info(s"Invalid payload or data mismatch")
        decode[AuthErrorResponse](response.json.toString) match {
          case Left(_) => ApplicationResult.error(ClientError("Decoding error"))
          case Right(error) if error.errorDescription == "Account disabled" =>
            ApplicationResult.error(ClientError("Account disabled"))
        }
      case 401 =>
        logger.info(s"Failing login into Auth server: Unauthorized.")
        decode[AuthErrorResponse](response.json.toString) match {
          case Left(_) => ApplicationResult.error(AuthenticationError("Decoding error"))
          case Right(error) if error.errorDescription == "Invalid user credentials" =>
            ApplicationResult.error(AuthenticationError("Invalid credentials"))
        }
      case _ =>
        logger.info("Failing connecting with auth server")
        ApplicationResult.error(EmptyResponse)
    }

  private def createAuthRequestBody(username: String, password: String, clientId: String) =
    formUrlEncodedBody(
      username,
      password,
      clientId,
      this.configuration.clientSecret,
      this.configuration.grantType,
      this.configuration.scope
    )

}
