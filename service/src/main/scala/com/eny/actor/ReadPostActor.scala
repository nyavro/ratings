package com.eny.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import com.eny.model._

import scala.concurrent.ExecutionContext.Implicits.global

case class Page(limit:Int, start:Option[PostID])
case class ListPostFollowers(post:PostID, limit:Int, start:Option[UserID])
case class ListPostResponses(post:PostID, limit:Int, start:Option[ResponseID])
case class GetPost(id:PostID, target: ActorRef)

class ReadPostActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case Page(limit, start) =>
      Posts.list(limit, start) pipeTo sender
    case GetPost(id, target) => Posts.getById(id) pipeTo target
  }

}
