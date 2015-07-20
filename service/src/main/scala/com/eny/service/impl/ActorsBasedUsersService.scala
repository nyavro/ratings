package com.eny.service.impl

import java.io.InputStream
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.eny.actor._
import com.eny.model._
import com.eny.service.{Initiator, UsersService}

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import akka.pattern.ask

import scala.util.Try

class ActorsBasedUsersService(system:ActorSystem) extends UsersService {

  implicit val timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS))

  lazy val read = system.actorOf(Props[ReadUserActor], "userRead")
  lazy val write = system.actorOf(Props[WriteUserActor], "profileWrite")


  override def findByUserIdAndProvider(userId: UserID, providerId: String): Future[Option[(UserDetails, SocialProfile)]] =
    (read ? ByIdAndProvider(userId, providerId)).mapTo[Option[(UserDetails, SocialProfile)]]

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[(UserDetails, SocialProfile)]] =
    (read ? ByEmailAndProvider(email, providerId)).mapTo[Option[(UserDetails, SocialProfile)]]

  override def findByUserId(userId: UserID): Future[Seq[SocialProfile]] =
    (read ? ById(userId)).mapTo[Seq[SocialProfile]]

  override def save(profile: SocialProfile): Future[Try[Boolean]] =
    (write ? SaveSocialProfile(profile)).mapTo[Try[Boolean]]

  override def findByExternalUserIdAndProvider(externalId: String, providerId: String): Future[Option[(UserDetails, SocialProfile)]] =
    (read ? ByExternalIdAndProvider(externalId, providerId)).mapTo[Option[(UserDetails, SocialProfile)]]

  override def save(user: UserDetails): Future[Try[Boolean]] =
    (write ? SaveUserDetails(user)).mapTo[Try[Boolean]]

  override def setAboutMe(userId: UserID, aboutme:String): Future[Try[Boolean]] =
    (write ? UpdateAboutMe(userId, aboutme)).mapTo[Try[Boolean]]

  override def uploadAvatar(userId: UserID, stream:InputStream): Future[Try[Boolean]] =
    (write ? UploadAvatar(userId, stream)).mapTo[Try[Boolean]]
}
