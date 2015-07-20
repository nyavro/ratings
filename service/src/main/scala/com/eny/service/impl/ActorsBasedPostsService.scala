package com.eny.service.impl


import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.eny.actor._
import com.eny.model._
import com.eny.model.bean.Post
import com.eny.service.{Initiator, PostsService}

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

class ActorsBasedPostsService(system:ActorSystem, MaxMessageLength:Int) extends PostsService {
  implicit val timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS))

  lazy val read = system.actorOf(Props[ReadPostActor])
  lazy val write = system.actorOf(WritePostActor.props(Posts, MaxMessageLength))

  override def update(postId: PostID, message: String, initiator:Initiator) =
    (write ? Update(postId, message, initiator)).mapTo[Try[Post]]

  override def setActive(postId: PostID, active: Boolean, initiator:Initiator) =
    (write ? SetActive(postId, active, initiator)).mapTo[Try[Post]]

  override def create(post: Post) =
    (write ? post).mapTo[Try[Post]]

  override def list(limit: Int, start: Option[PostID]) =
    (read ? Page(limit, start)).mapTo[List[Post]]
}
