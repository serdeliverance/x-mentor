package models.configurations

class AuthConfiguration(
    val urls: AuthUrlsConfiguration,
    val users: UsersConfiguration,
    val clientId: String,
    val clientSecret: String,
    val grantType: String,
    val scope: String,
    val publicKey: String)

class AuthUrlsConfiguration(
    base: String,
    token: String,
    users: String,
    setPassword: String,
    modifyPassword: String,
    setRoles: String,
    logout: String) {

  private val idLabel = ":id"

  val tokenUrl = s"$base$token"

  val usersUrl = s"$base$users"

  def userUrl(id: String): String = s"$usersUrl/$id"

  def setPasswordUrl(id: String): String = s"$base$setPassword".replace(idLabel, id)

  def modifyPasswordUrl: String = s"$base$modifyPassword"

  def setRolesUrl(id: String): String = s"$base$setRoles".replace(idLabel, id)

  def logoutUrl(id: String): String = s"$base$logout".replace(idLabel, id)

}

class UsersConfiguration(val admin: UserConfiguration, val usernameAttributeName: String)

class UserConfiguration(val username: String, val password: String)
