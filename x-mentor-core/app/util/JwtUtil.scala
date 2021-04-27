package util

import constants.PUBLIC_KEY
import pdi.jwt.{Jwt, JwtAlgorithm}
import repositories.RedisRepository
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt
import scala.concurrent.{CanAwait, ExecutionContext}

trait JwtUtil extends RedisRepository {

  private val authPublicKey = get(PUBLIC_KEY)
  private val jwtAlgorithm  = Seq(JwtAlgorithm.RS256)

  /**
    * Decodes a jwt into its json string representation
    *
    * @param jwt
    * @return
    */
  def decode(jwt: String)(implicit ec: ExecutionContext, ca: CanAwait): Option[String] =
    Jwt.decodeRaw(jwt, authPublicKey.mapTo[String].result(10 seconds), jwtAlgorithm).toOption

  /**
    * Validates token signature using the auth server's public certificate
    *get value f
    * @param token
    * @return
    */
  def hasValidSignature(token: String)(implicit ec: ExecutionContext, ca: CanAwait): Boolean =
    Jwt.isValid(token, authPublicKey.mapTo[String].result(10 seconds), jwtAlgorithm)

}
