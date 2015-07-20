package service

import _root_.java.util.Date

import securesocial.core._

/**
 * Created by eny on 30.01.15.
 */
class ExtendedProfile(
    override val providerId : String,
    override val userId : String,
    override val firstName : Option[String],
    override val lastName : Option[String],
    override val fullName : Option[String],
    override val email : Option[String],
    override val avatarUrl : Option[String],
    override val authMethod : AuthenticationMethod,
    override val oAuth1Info : Option[OAuth1Info],
    override val oAuth2Info : Option[OAuth2Info],
    override val passwordInfo : Option[PasswordInfo],
    val gender: Option[String],
    val birthDate: Option[Date],
    val city: Option[String]
  ) extends
  BasicProfile(providerId, userId, firstName, lastName, fullName, email, avatarUrl, authMethod, oAuth1Info, oAuth2Info, passwordInfo)
