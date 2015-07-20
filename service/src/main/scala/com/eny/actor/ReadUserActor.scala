package com.eny.actor

import java.util.{Date, UUID}

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.eny.model._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ByIdAndProvider(id:UserID, provider:String)
case class ByEmailAndProvider(email:String, provider:String)
case class ById(id:UserID)
case class ByExternalIdAndProvider(externalId:String, provider:String)

class ReadUserActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case ByIdAndProvider(id, provider) => ret(SocialProfiles.findByIdAndProvider(id,provider)) pipeTo sender
    case ByEmailAndProvider(email, provider) => ret(SocialProfiles.findByEmailAndProvider(email, provider)) pipeTo sender
    case ById(id) => SocialProfiles.findById(id) pipeTo sender
    case ByExternalIdAndProvider(externalId, provider) => ret(SocialProfiles.findByExternalIdAndProvider(externalId, provider)) pipeTo sender
  }

  def ret(retrieve: Future[Option[SocialProfile]]): Future[Option[(UserDetails, SocialProfile)]] =
    for (
      profile <- retrieve;
      user <- profile.map(item => UserDetailsTable.findById(item.id)).getOrElse(Future.successful(None))
    ) yield {
      for (
        usr <- user;
        prfl <- profile
      ) yield (usr, prfl)
    }
}
