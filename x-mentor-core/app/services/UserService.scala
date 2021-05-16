package services

import akka.Done
import cats.data.EitherT
import global.ApplicationResult
import io.circe.parser.decode
import models.auth.{AccessData, AuthErrorResponse}
import models.configurations.AuthConfiguration
import models.errors.{AuthenticationError, ClientError, EmptyResponse, NotFoundError, UserAlreadyExistsError}
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
import constants.{STUDENT_LABEL, STUDENT_PROGRESS_KEY_PREFIX, STUDENT_PROGRESS_LIST_KEY, USERS_FILTER}
import models.Student
import play.api.libs.json._
import repositories.{RedisBloomRepository, RedisRepository, RedisTimeSeriesRepository}
import repositories.graph.StudentRepository

@Singleton
class UserService @Inject()(
    sender: Sender,
    configuration: AuthConfiguration,
    redisBloomRepository: RedisBloomRepository,
    studentRepository: StudentRepository,
    redisTimeSeriesRepository: RedisTimeSeriesRepository,
    redisRepository: RedisRepository
  )(implicit ec: ExecutionContext)
    extends Logging
    with CirceImplicits {

  /**
    * Starts the authentication process against Keycloak
    *
    * 1. Verifies if user's username already exists in [[constants.USERS_FILTER]] bloom filter
    * 2. Gets auth token
    */
  def login(
      username: String,
      password: String
    )(implicit mapMarkerContext: MapMarkerContext
    ): ApplicationResult[AccessData] = {
    logger.info(s"login with user: $username")

    val reqBody    = createAuthRequestBody(username, password)
    val reqHeaders = List((HeaderNames.CONTENT_TYPE, MimeTypes.FORM))

    for {
      exists <- EitherT(redisBloomRepository.exists(USERS_FILTER, username))
      authResponse <- EitherT {
        if (exists) sender.post(this.configuration.urls.tokenUrl, reqBody, reqHeaders)
        else ApplicationResult.error(NotFoundError("User does not exists"))
      }
      accessData <- EitherT { handleAuthResponse(authResponse) }
    } yield accessData
  }.value

  /**
    * Starts the registration process against Keycloak
    *
    * 1. Adds user's username to [[constants.USERS_FILTER]] bloom filter
    * 2. Creates user in redisGraph
    *
    */
  def signup(
      username: String,
      password: String
    )(implicit mapMarkerContext: MapMarkerContext
    ): ApplicationResult[Done] = {
    logger.info(s"Creating user: $username")

    val requestTokenBody =
      createAuthRequestBody(this.configuration.users.admin.username, this.configuration.users.admin.password)
    val requestTokenHeaders = List((HeaderNames.CONTENT_TYPE, MimeTypes.FORM))

    val createUserBody = Json.obj(
      "username"    -> username,
      "enabled"     -> true,
      "credentials" -> Json.arr(Json.obj("type" -> "password", "value" -> username, "temporary" -> false))
    )
    val createUserHeaders = List((HeaderNames.CONTENT_TYPE, MimeTypes.JSON))

    for {
      authResponse <- EitherT {
        sender.post(this.configuration.urls.tokenUrl, requestTokenBody, requestTokenHeaders)
      }
      adminToken <- EitherT { handleAuthResponse(authResponse) }
      creationResponse <- EitherT {
        sender.post(this.configuration.urls.usersUrl,
                    createUserBody,
                    createUserHeaders.appended((HeaderNames.AUTHORIZATION, s"Bearer ${adminToken.accessToken}")))
      }
      response <- EitherT { handleCreationResponse(creationResponse) }
      _        <- EitherT { redisBloomRepository.add(USERS_FILTER, username) }
      _        <- EitherT { studentRepository.createStudent(Student(username, s"$username@gmail.com")) }
      studentProgressTimeSeriesKey = s"$STUDENT_PROGRESS_KEY_PREFIX:$username"
      _ <- EitherT {
        redisTimeSeriesRepository.create(studentProgressTimeSeriesKey, Map(STUDENT_LABEL -> username))
      }
      _ <- EitherT {
        redisRepository.rpush(STUDENT_PROGRESS_LIST_KEY, studentProgressTimeSeriesKey)
      }
    } yield response
  }.value

  /**
    * Handles the response from Auth service and matches it with the corresponding [[ApplicationResult]]
    *
    * @param response wsResponse
    * @return
    */
  private def handleAuthResponse(response: WSResponse): ApplicationResult[AccessData] =
    response.status match {
      case 200 =>
        logger.info(s"Login Successful")
        decode[AccessData](response.body)
          .fold(
            error => {
              logger.error(s"Error parsing auth server response $error")
              ApplicationResult.error(ClientError(s"Error parsing auth server response: $error"))
            },
            accessData => ApplicationResult(accessData)
          )
      case 400 =>
        logger.warn(s"Invalid payload or data mismatch")
        decode[AuthErrorResponse](response.json.toString) match {
          case Left(_) => ApplicationResult.error(ClientError("Decoding error"))
          case Right(error) if error.errorDescription == "Account disabled" =>
            ApplicationResult.error(ClientError("Account disabled"))
        }
      case 401 =>
        logger.warn(s"Failing login into Auth server: Unauthorized.")
        decode[AuthErrorResponse](response.json.toString) match {
          case Left(_) => ApplicationResult.error(AuthenticationError("Decoding error"))
          case Right(error) if error.errorDescription == "Invalid user credentials" =>
            ApplicationResult.error(AuthenticationError("Invalid credentials"))
        }
      case _ =>
        logger.warn("Failing connecting with auth server")
        ApplicationResult.error(EmptyResponse)
    }

  private def createAuthRequestBody(username: String, password: String) =
    formUrlEncodedBody(
      username,
      password,
      this.configuration.clientId,
      this.configuration.clientSecret,
      this.configuration.grantType,
      this.configuration.scope
    )

  private def handleCreationResponse(response: WSResponse): ApplicationResult[Done] =
    response.status match {
      case 201 =>
        logger.info(s"User successfully created")
        ApplicationResult(Done)
      case 400 =>
        logger.warn(s"Invalid payload or data mismatch")
        decode[AuthErrorResponse](response.json.toString) match {
          case Left(_) => ApplicationResult.error(ClientError("Decoding error"))
          case Right(error) if error.errorDescription == "Account disabled" =>
            ApplicationResult.error(ClientError("Account disabled"))
        }
      case 401 =>
        logger.warn(s"Failing login into Auth server: Unauthorized.")
        decode[AuthErrorResponse](response.json.toString) match {
          case Left(_) => ApplicationResult.error(AuthenticationError("Decoding error"))
          case Right(error) if error.errorDescription == "Invalid user credentials" =>
            ApplicationResult.error(AuthenticationError("Invalid credentials"))
        }
      case 409 =>
        logger.warn("User already exists")
        ApplicationResult.error(UserAlreadyExistsError("User already exists"))
      case _ =>
        logger.warn("Failing connecting with auth server")
        ApplicationResult.error(EmptyResponse)
    }

}
