package util

import pdi.jwt.{Jwt, JwtAlgorithm}
import scala.language.postfixOps

trait JwtUtil {

  private val jwtAlgorithm  = Seq(JwtAlgorithm.RS256)

  /**
    * Decodes a jwt into its json string representation
    *
    * @param jwt
    * @return
    */
  def decode(jwt: String, authPublicKey: String): Option[String] =
    Jwt.decodeRaw(jwt, authPublicKey, jwtAlgorithm).toOption

  /**
    * Validates token signature using the auth server's public certificate
    *get value f
    * @param token
    * @return
    */
  def hasValidSignature(token: String, authPublicKey: String): Boolean =
    Jwt.isValid(token, authPublicKey, jwtAlgorithm)

}
