package service

import _root_.java.util.UUID

import com.eny.model.{SocialProfile, UserDetails, UserID}
import com.eny.service.Role.Role
import com.eny.service.{Role, UsersService}
import play.api.Logger
import securesocial.core._
import securesocial.core.providers.{MailToken, UsernamePasswordProvider}
import securesocial.core.services.{SaveMode, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PersistentUserService(service: UsersService) extends UserService[MultiProfileUser] {
  val logger = Logger("application.controllers.PersistentUserService")

  private var tokens = Map[String, MailToken]()

  def convert(details: UserDetails, profile: SocialProfile): BasicProfile =
    BasicProfile(
      profile.socialHandle,
      profile.externalId,
      details.firstName,
      details.lastName,
      details.fullName,
      profile.email,
      details.avatarUrl,
      AuthenticationMethod(profile.authMethod),
      for(
        token <- profile.oauth1token;
        secret <- profile.oauth1secret
      ) yield OAuth1Info(token, secret),
      profile.oauth2token.map(token => OAuth2Info(token, profile.oauth2tokenType, profile.oauth2expiresIn, profile.oauth2refreshToken)),
      for(
        algorithm <- profile.algorithm;
        password <- profile.password
      ) yield PasswordInfo(algorithm, password, profile.salt)
    )

  def simplify[T](arg:Option[Option[T]]):Option[T] =
    arg match {
      case None => None
      case Some(x) => x
    }
  
  def convert(profile: BasicProfile, userId: UserID): (UserDetails, SocialProfile) = {
    (
      profile match {
        case ext : ExtendedProfile =>
          UserDetails(
            id = userId,
            gender = ext.gender,
            birthDate = ext.birthDate,
            city = ext.city,
            firstName = ext.firstName,
            lastName = ext.lastName,
            fullName = ext.fullName,
            email = ext.email,
            avatarUrl = ext.avatarUrl,
            roles = Set(Role.User.toString),
            banned = false,
            aboutme = None
          )
        case _ =>
          UserDetails(
            id = userId,
            gender = None,
            birthDate = None,
            city = None,
            firstName = profile.firstName,
            lastName = profile.lastName,
            fullName = profile.fullName,
            email = profile.email,
            avatarUrl = profile.avatarUrl,
            roles = Set(Role.User.toString),
            banned = false,
            aboutme = None
          )
      },
      SocialProfile(
        id = userId,
        externalId = profile.userId,
        socialHandle = profile.providerId,
        authMethod = profile.authMethod.method,
        email = profile.email,
        oauth1token = profile.oAuth1Info.map(_.token),
        oauth1secret = profile.oAuth1Info.map(_.secret),
        oauth2token = profile.oAuth2Info.map(_.accessToken),
        oauth2tokenType = simplify(profile.oAuth2Info.map(_.tokenType)),
        oauth2expiresIn = simplify(profile.oAuth2Info.map(_.expiresIn)),
        oauth2refreshToken = simplify(profile.oAuth2Info.map(_.refreshToken)),
        algorithm = profile.passwordInfo.map(_.hasher),
        password = profile.passwordInfo.map(_.password),
        salt = simplify(profile.passwordInfo.map(_.salt))
      )
    )
  }

  def find(providerId: String, externalId: String): Future[Option[BasicProfile]] = {
    if (logger.isDebugEnabled) logger.debug(s"Searching users with providerId = $providerId and userId = $externalId")
    for (
      found <- service.findByExternalUserIdAndProvider(externalId, providerId)
    ) yield found.map {case (details, profile) => convert(details, profile)}
  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = {
    if (logger.isDebugEnabled) logger.debug(s"Searching users with providerId = $providerId and email = $email")
    for (
      found <- service.findByEmailAndProvider(email, providerId)
    ) yield found.map{case (details, profile) => convert(details, profile)}
  }

  private def findProfile(basic: BasicProfile):Future[Option[MultiProfileUser]] = {
    for(
      found <- service.findByExternalUserIdAndProvider(basic.userId, basic.providerId);
      identities <- found match {
        case Some((details, _)) => service.findByUserId(details.id)
        case None => Future.successful(Seq())
      }
    ) yield {
      found.map {case (details, profile) => MultiProfileUser(details.id, basic, identities.map(item => convert(details, item)), details.roles.map(item => Role.withName(item)))}
    }
  }

  def save(user: BasicProfile, mode: SaveMode): Future[MultiProfileUser] = {
    if (logger.isDebugEnabled) logger.debug(s"Saving user = $user with mode = $mode")
    mode match {
      case SaveMode.SignUp =>
        val id: UserID = UUID.randomUUID()
        val (details, profile) =  convert(user, id)
        for (
          savedDetails <- service.save(details);
          savedProfile <- service.save(profile)
        ) yield MultiProfileUser(id, user, List(user), Set(Role.User))
      case SaveMode.LoggedIn =>
        // first see if there is a user with this BasicProfile already.
        for (
          find <- findProfile(user);
          saved <- find match {
            case Some(multiProfileUser) =>
              val id = multiProfileUser.userId
              val (details, profile) = convert(user, id)
              for (
                savedDetails <- service.save(details);
                savedProfile <- service.save(profile)
              ) yield MultiProfileUser(id, user, multiProfileUser.identities, multiProfileUser.roles)
            case None =>
              val id: UserID = UUID.randomUUID()
              val (details, profile) = convert(user, id)
              for (
                savedDetails <- service.save(details);
                savedProfile <- service.save(profile)
              ) yield MultiProfileUser(id, user, List(user), Set(Role.User))
          }
        ) yield saved
      case SaveMode.PasswordChange =>
        for (
          find <- findProfile(user);
          saved <- find match {
            case Some(multiProfileUser) =>
              val (details, profile) = convert(user, multiProfileUser.userId)
              for (
                savedDetails <- service.save(details);
                savedProfile <- service.save(profile)
              ) yield MultiProfileUser(multiProfileUser.userId, user, multiProfileUser.identities, multiProfileUser.roles)
            case None => throw new Exception("missing profile)")
          }
        ) yield saved
    }
  }

  def link(current: MultiProfileUser, to: BasicProfile): Future[MultiProfileUser] = {
    if (logger.isDebugEnabled) logger.debug(s"Linking $current to $to")
    if (current.identities.exists(item => item.providerId == to.providerId && item.userId == to.userId)) {
      Future.successful(current)
    } else {
      val added = Seq(to) ++ current.identities
      val (details, profile) = convert(to, current.userId)
      for(
        savedDetails <- service.save(details);
        savedProfile <- service.save(profile)
      ) yield current.copy(identities = added)
    }
  }

  def saveToken(token: MailToken): Future[MailToken] = {
    Future.successful {
      tokens += (token.uuid -> token)
      token
    }
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    Future.successful { tokens.get(token) }
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(uuid) match {
        case Some(token) =>
          tokens -= uuid
          Some(token)
        case None => None
      }
    }
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }

  override def updatePasswordInfo(user: MultiProfileUser, info: PasswordInfo): Future[Option[BasicProfile]] = {
    for(
      found <- service.findByUserIdAndProvider(user.userId, UsernamePasswordProvider.UsernamePassword);
      updated <- found match {
        case Some((details, profile)) =>
          service.save(profile.copy(algorithm = Some(info.hasher), password = Some(info.password), salt = info.salt))
            .map(item=>item.toOption.flatMap(item=>if(item) Some(convert(details, profile)) else None))
        case _ => Future.successful(None)
      }
    ) yield updated
  }

  override def passwordInfoFor(user: MultiProfileUser): Future[Option[PasswordInfo]] = {
    for(
      found <- service.findByUserIdAndProvider(user.userId, UsernamePasswordProvider.UsernamePassword)
    ) yield {
      simplify(
        found.map {
          case (details, profile) =>
            for (
              algorithm <- profile.algorithm;
              password <- profile.password
            ) yield PasswordInfo(algorithm, password, profile.salt)
        }
      )
    }
  }
}

case class MultiProfileUser(userId:UserID, main: BasicProfile, identities: Seq[BasicProfile], roles:Set[Role])
