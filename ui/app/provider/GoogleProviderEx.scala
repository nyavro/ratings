package provider

import _root_.java.util.Date

import play.api.libs.json.{JsArray, JsObject}
import securesocial.core._
import securesocial.core.providers.GoogleProvider
import securesocial.core.services.{CacheService, RoutesService}
import service.ExtendedProfile

import scala.concurrent.Future

class GoogleProviderEx(
  routesService: RoutesService,
  cacheService: CacheService,
  client: OAuth2Client) extends GoogleProvider(routesService, cacheService, client) {

  override val UserInfoApi = "https://www.googleapis.com/plus/v1/people/me?fields=id,name,displayName,image,emails,birthday,occupation,gender&access_token="

  val Gender = "gender"
  val City = "occupation"
  val Birthday = "birthday"

  override def fillProfile(info: OAuth2Info): Future[BasicProfile] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val accessToken = info.accessToken
    client.retrieveProfile(UserInfoApi + accessToken).map { me =>
      (me \ Error).asOpt[JsObject] match {
        case Some(error) =>
          val message = (error \ Message).as[String]
          val errorCode = (error \ Code).as[String]
          logger.error(s"[securesocial] error retrieving profile information from Google. Error type = $errorCode, message = $message")
          throw new AuthenticationException()
        case _ =>
          val emails = (me \ Emails).asInstanceOf[JsArray]
          new ExtendedProfile(
            providerId = id,
            userId = (me \ Id).as[String],
            firstName = (me \ Name \ GivenName).asOpt[String],
            lastName = (me \ Name \ FamilyName).asOpt[String],
            fullName = (me \ DisplayName).asOpt[String],
            email = emails.value.find(v => (v \ EmailType).as[String] == Account).map(e => (e \ Email).as[String]),
            avatarUrl = (me \ Image \ Url).asOpt[String],
            authMethod = authMethod,
            oAuth2Info = Some(info),
            oAuth1Info = None,
            passwordInfo = None,
            gender = (me \ Gender).asOpt[String],
            city = (me \ City).asOpt[String],
            birthDate = (me \ Birthday).asOpt[Date]
          )
      }
    } recover {
      case e: AuthenticationException => throw e
      case e =>
        logger.error("[securesocial] error retrieving profile information from Google", e)
        throw new AuthenticationException()
    }
  }
}