package com.eny.actor

import java.io.InputStream

import akka.actor.{Actor, ActorLogging}
import com.eny.model._
import akka.pattern.pipe
import com.eny.service.{Role, Initiator}
import com.eny.service.Role.Role
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

case class SetRole(userId:UserID, role:Role, initiator:Initiator)
case class Ban(userId:UserID, value:Boolean, initiator:Initiator)
case class UpdateAboutMe(userId:UserID, aboutme:String)
case class SaveUserDetails(details:UserDetails)
case class UploadAvatar(userId: UserID, stream:InputStream)
case class SaveSocialProfile(profile:SocialProfile)

class WriteUserActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case SaveSocialProfile(profile) => SocialProfiles.insertRecord(profile).map(res => Success(res)) pipeTo sender
    case SetRole(userId, role, initiator) => (
      if(!initiator.roles.contains(Role.Super)) {
        Future.successful(Failure(new IllegalStateException("Not enough permissions to set user role")))
      }
      else {
        for {
          userProfile <- UserDetailsTable.findById(userId)
          update <- userProfile match {
            case None => Future.successful(Failure(new IllegalArgumentException(s"User with id='$userId' not found")))
            case Some(usr) => UserDetailsTable.addRole(userId, role.toString).map(result => Success(result))
          }
        } yield update
      }
    ) pipeTo sender
    case Ban(userId, value, initiator) => (
      if(initiator.roles.contains(Role.Super) || initiator.roles.contains(Role.Admin))
        for(
          userProfile <- UserDetailsTable.findById(userId);
          update <- userProfile match {
            case None => Future.successful(Failure(new IllegalArgumentException(s"User with id='$userId' not found")))
            case Some(usr) => UserDetailsTable.ban(userId, value).map(result => Success(result))
          }
        ) yield update
      else {
        Future.successful(Failure(new IllegalStateException("Not enough permissions to perform action")))
      }
    ) pipeTo sender
    case SaveUserDetails(details) => UserDetailsTable.insertRecord(details).map(item => Success(item)) pipeTo sender
    case UpdateAboutMe(id, aboutme) => UserDetailsTable.setAboutme(id, Some(aboutme)).map(item => Success(item)) pipeTo sender
  }
}
