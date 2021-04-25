package util

import com.typesafe.config.ConfigFactory
import configurations.AUTH_PUBLIC_KEY
import pdi.jwt.{Jwt, JwtAlgorithm}

trait JwtUtil {

  private val authPublicKey = ConfigFactory.load().getString(AUTH_PUBLIC_KEY)
  private val jwtAlgorithm  = Seq(JwtAlgorithm.RS256)

  /**
    * Decodes a jwt into its json string representation
    *
    * @param jwt
    * @return
    */
  def decode(jwt: String): Option[String] =
    Jwt.decodeRaw(jwt, authPublicKey, jwtAlgorithm).toOption

  /**
    * Validates token signature using the auth server's public certificate
    *
    * @param token
    * @return
    */
  def hasValidSignature(token: String): Boolean =
    Jwt.isValid(token, authPublicKey, jwtAlgorithm)
}
