package com.eny.model

import scala.concurrent.Future


case class SocialProfile(
  id: SocialProfileID,
  externalId: String,
  socialHandle: String,
  authMethod: String,
  email: Option[String],
  oauth1token: Option[String] = None,
  oauth1secret: Option[String] = None,
  oauth2token: Option[String] = None,
  oauth2tokenType: Option[String] = None,
  oauth2expiresIn: Option[Int] = None,
  oauth2refreshToken: Option[String] = None,
  algorithm: Option[String] = None,
  password: Option[String] = None,
  salt: Option[String] = None
)

object SocialProfiles {

  var profiles = List[SocialProfile]()
  
  def insertRecord(profile: SocialProfile) = {
    profiles = profile::profiles
    Future.successful(true)
  }
  
  def findByExternalIdAndProvider(externalId:String, provider:String) = 
    Future.successful(
      profiles.find(
        profile => profile.externalId == externalId
      )
    )
  
  def findById(id: SocialProfileID) =
    Future.successful(
      profiles.filter(
        profile => profile.id == id
      )
    )

  def findByIdAndProvider(id:SocialProfileID, provider:String) =
    Future.successful(
      profiles.find(
        profile => profile.id==id && profile.socialHandle==provider
      )
    )

  def findByEmailAndProvider(email: String, provider: String) =
    Future.successful(
      profiles.find(
        profile => profile.email.getOrElse("")==email && profile.socialHandle==provider
      )
    )

  def findByIds(ids: List[SocialProfileID]) =
    Future.successful(
      profiles.filter(
        profile => ids.contains(profile.id)
      )
    )
}
