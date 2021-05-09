package models.configurations

class AuthConfiguration(
    val urls: AuthUrlsConfiguration,
    val users: UsersConfiguration,
    val clientId: String,
    val clientSecret: String,
    val grantType: String,
    val scope: String)

class AuthUrlsConfiguration(
    base: String,
    token: String,
    users: String,
    realm: String,
    logout: String) {

  private val idLabel = ":id"

  val realmUrl = s"$base$realm"

  val tokenUrl = s"$base$token"

  val usersUrl = s"$base$users"

  def userUrl(id: String): String = s"$usersUrl/$id"

  def logoutUrl(id: String): String = s"$base$logout".replace(idLabel, id)

}

class UsersConfiguration(val admin: UserConfiguration, val usernameAttributeName: String)

class UserConfiguration(val username: String, val password: String)
