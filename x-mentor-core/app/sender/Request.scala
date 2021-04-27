package sender

object Request {

  private val CLIENT_ID     = "client_id"
  private val CLIENT_SECRET = "client_secret"
  private val GRANT_TYPE    = "grant_type"
  private val SCOPE         = "scope"

  /**
    * Builds a request body to be sent in a request with x-www-form-urlencoded Content-Type.
    *
    * @param username
    * @param password
    * @param clientId
    * @param clientSecret
    * @param grantType
    * @param scope
    * @return
    */
  def formUrlEncodedBody(
      username: String,
      password: String,
      clientId: String,
      clientSecret: String,
      grantType: String,
      scope: String
    ): String =
    Map(
      CLIENT_ID     -> clientId,
      CLIENT_SECRET -> clientSecret,
      GRANT_TYPE    -> grantType,
      "username"    -> username,
      "password"    -> password,
      SCOPE         -> scope
    ).map { case (k, v) => s"$k=$v" }.mkString("&")

  /**
    * Builds a request body to be sent in a request with x-www-form-urlencoded Content-Type.
    *
    * @param subjectToken
    * @param requestedSubject
    * @param clientId
    * @param clientSecret
    * @param grantType
    * @param scope
    * @return
    */
  def formUrlEncodedBodyTokenExchange(
      subjectToken: String,
      requestedSubject: String,
      clientId: String,
      clientSecret: String,
      grantType: String,
      scope: String
    ): String =
    Map(
      CLIENT_ID           -> clientId,
      CLIENT_SECRET       -> clientSecret,
      GRANT_TYPE          -> grantType,
      "subject_token"     -> subjectToken,
      "requested_subject" -> requestedSubject,
      SCOPE               -> scope
    ).map { case (k, v) => s"$k=$v" }.mkString("&")

}
