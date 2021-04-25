package controllers.actions

import play.api.Logging
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, BodyParser, BodyParsers, Request, WrappedRequest}
import util.JwtUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuthenticatedAction @Inject()(val parser: BodyParsers.Default)(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent]
    with ActionRefiner[Request, UserRequest]
    with Logging
    with JwtUtil {}

case class UserRequest[A](siteId: String, request: Request[A]) extends WrappedRequest[A](request)
