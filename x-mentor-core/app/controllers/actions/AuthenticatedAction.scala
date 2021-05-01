package controllers.actions

import akka.Done
import cats.data.EitherT
import constants.{AUTHORIZATION_BEARER_PREFIX, AUTHORIZATION_HEADER, ID_TOKEN_HEADER, PUBLIC_KEY}
import models.configurations.AuthConfiguration
import play.api.Logging
import play.api.mvc.Results.{BadRequest, Unauthorized}
import play.api.mvc._
import util.{JsonUtils, JwtUtil}
import javax.inject.{Inject, Singleton}
import repositories.RedisRepository

import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._

@Singleton
class AuthenticatedAction @Inject()(
    val parser: BodyParsers.Default,
    authConfiguration: AuthConfiguration,
    redisRepository: RedisRepository
  )(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent]
    with ActionRefiner[Request, UserRequest]
    with Logging
    with JwtUtil
    with JsonUtils {

  def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    for {
      publicKey   <- EitherT(getKey)
      _           <- EitherT(accessTokenValidation(request, publicKey))
      _           <- EitherT(idTokenValidation(request, publicKey))
      userRequest <- EitherT(userAction(request, publicKey))
    } yield userRequest
  }.value

  private def getKey: Future[Either[Result, String]] =
    redisRepository.get(PUBLIC_KEY).map {
      case Some(publicKey) => Right(publicKey)
      case None            => Left(BadRequest)
    }

  private def accessTokenValidation[A](request: Request[A], publicKey: String): Future[Either[Result, Done]] =
    Future.successful {
      request.headers
        .get(AUTHORIZATION_HEADER)
        .map(extractJwt) match {
        case Some(tokenValue) if hasValidSignature(tokenValue, publicKey) =>
          logger.info("Access token validation success.")
          Right(Done)
        case None =>
          logger.info("Unauthorized. No authorization header provided.")
          Left(Unauthorized)
        case _ =>
          logger.info("Unauthorized. Access token signature is not valid.")
          Left(Unauthorized)
      }
    }

  private def idTokenValidation[A](request: Request[A], publicKey: String): Future[Either[Result, Done]] =
    Future.successful {
      request.headers
        .get(ID_TOKEN_HEADER) match {
        case Some(tokenValue) if hasValidSignature(tokenValue, publicKey) =>
          logger.info("The Id-Token validation was success.")
          Right(Done)
        case None =>
          logger.info("Unauthorized. No Id-Token header provided.")
          Left(Unauthorized)
        case _ =>
          logger.info("Unauthorized. Id-Token signature is not valid.")
          Left(Unauthorized)
      }
    }

  private def userAction[A](request: Request[A], publicKey: String): Future[Either[Result, UserRequest[A]]] =
    Future.successful {
      request.headers
        .get(ID_TOKEN_HEADER)
        .flatMap(jwt => decode(jwt, publicKey))
        .flatMap(json => extractValue[String](json, authConfiguration.users.usernameAttributeName)) match {
        case Some(username) =>
          Right(UserRequest(username, request))
        case None =>
          logger.error("No username header provided")
          Left(BadRequest)
      }
    }

  private def handleResponse(response: Option[String]): Future[Either[Result, String]] =
    response match {
      case Some(publicKey) => Future(Right(publicKey))
      case _ =>
        logger.info("Error retrieving public key")
        Future(Left(Unauthorized))
    }

  def executionContext: ExecutionContext = ec

  private def extractJwt(headerValue: String) =
    headerValue.stripPrefix(AUTHORIZATION_BEARER_PREFIX)
}

case class UserRequest[A](student: String, request: Request[A]) extends WrappedRequest[A](request)
