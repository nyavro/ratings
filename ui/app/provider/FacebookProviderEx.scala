package provider

import java.util.Date

import play.api.libs.json.JsObject
import securesocial.core.{AuthenticationException, BasicProfile, OAuth2Info, OAuth2Client}
import securesocial.core.providers.FacebookProvider
import securesocial.core.services.{CacheService, RoutesService}
import service.ExtendedProfile
import scala.concurrent.Future

/**
 * Created by eny on 02.02.15.
 */
class FacebookProviderEx(
  routesService: RoutesService,
  cacheService: CacheService,
  client: OAuth2Client) extends FacebookProvider(routesService, cacheService, client) {

  val Gender = "gender"
  val City = "location"
  val Birthday = "birthday"

  override val MeApi = "https://graph.facebook.com/me?fields=name,first_name,last_name,picture.type(large),email,gender,location,birthday&return_ssl_resources=1&access_token="

  override def fillProfile(info: OAuth2Info): Future[BasicProfile] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val accessToken = info.accessToken
    client.retrieveProfile(MeApi + accessToken).map { me =>
      (me \ Error).asOpt[JsObject] match {
        case Some(error) =>
          val message = (error \ Message).as[String]
          val errorType = (error \ Type).as[String]
          logger.error(
            "[securesocial] error retrieving profile information from Facebook. Error type: %s, message: %s".
              format(errorType, message)
          )
          throw new AuthenticationException()
        case _ =>
          new ExtendedProfile(
            providerId = id,
            userId = (me \ Id).as[String],
            firstName = (me \ FirstName).asOpt[String],
            lastName = (me \ LastName).asOpt[String],
            fullName = (me \ Name).asOpt[String],
            email = (me \ Email).asOpt[String],
            avatarUrl = (me \ Picture \ Data \ Url).asOpt[String],
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
        logger.error("[securesocial] error retrieving profile information from Facebook", e)
        throw new AuthenticationException()
    }
  }
}
