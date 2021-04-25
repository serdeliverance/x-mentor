package controllers.actions

import akka.Done
import cats.data.EitherT
import cats.implicits._
import constants.{AUTHORIZATION_BEARER_PREFIX, AUTHORIZATION_HEADER, ID_TOKEN_HEADER}
import models.configurations.AuthConfiguration
import play.api.Logging
import play.api.mvc.Results.{BadRequest, Unauthorized}
import play.api.mvc._
import util.{JsonUtils, JwtUtil}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedAction @Inject()(
    val parser: BodyParsers.Default,
    authConfiguration: AuthConfiguration
  )(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent]
    with ActionRefiner[Request, UserRequest]
    with Logging
    with JwtUtil
    with JsonUtils {

  def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    for {
      _           <- EitherT(accessTokenValidation(request))
      _           <- EitherT(idTokenValidation(request))
      userRequest <- EitherT(userAction(request))
    } yield userRequest
  }.value

  private def accessTokenValidation[A](request: Request[A]): Future[Either[Result, Done]] = Future.successful {
    request.headers
      .get(AUTHORIZATION_HEADER)
      .map(extractJwt) match {
      case Some(tokenValue) if hasValidSignature(tokenValue) =>
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

  private def idTokenValidation[A](request: Request[A]): Future[Either[Result, Done]] = Future.successful {
    request.headers
      .get(ID_TOKEN_HEADER) match {
      case Some(tokenValue) if hasValidSignature(tokenValue) =>
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

  private def userAction[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = Future.successful {
    request.headers
      .get(ID_TOKEN_HEADER)
      .flatMap(jwt => decode(jwt))
      .flatMap(json => extractValue[String](json, authConfiguration.users.usernameAttributeName)) match {
      case Some(username) =>
        Right(UserRequest(username, request))
      case None =>
        logger.error("No username header provided")
        Left(BadRequest)
    }
  }

  def executionContext: ExecutionContext = ec

  private def extractJwt(headerValue: String) =
    headerValue.stripPrefix(AUTHORIZATION_BEARER_PREFIX)
}

case class UserRequest[A](student: String, request: Request[A]) extends WrappedRequest[A](request)
