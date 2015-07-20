package com.eny.service

import java.io.InputStream

import com.eny.model._

import scala.concurrent.Future
import scala.util.Try

trait UsersService {

  def findByUserIdAndProvider(userId: UserID, providerId: String): Future[Option[(UserDetails, SocialProfile)]]

  def findByUserId(userId: UserID): Future[Seq[SocialProfile]]

  def save(profile: SocialProfile): Future[Try[Boolean]]

  def findByExternalUserIdAndProvider(externalId: String, providerId: String): Future[Option[(UserDetails, SocialProfile)]]

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[(UserDetails, SocialProfile)]]

  def uploadAvatar(userId: UserID, stream:InputStream): Future[Try[Boolean]]
                                                                          
  def setAboutMe(userId: UserID, aboutme:String): Future[Try[Boolean]]

  def save(user: UserDetails): Future[Try[Boolean]]
}
