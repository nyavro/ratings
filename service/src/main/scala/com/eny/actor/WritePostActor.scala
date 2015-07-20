package com.eny.actor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.eny.model._
import com.eny.model.bean.{Post}
import com.eny.repository.PostRepository
import com.eny.service.{AccessDeniedException, Initiator, Role}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class Update(postId:PostID, text:String, initiator:Initiator)
case class SetActive(postId:PostID, active:Boolean, initiator:Initiator)
case class Follow(id:PostID, userId:UserID)

class WritePostActor(repository:PostRepository, MaxMessageLength: Int)
  extends Actor with ActorLogging {

  override def receive: Receive = {
    case Update(postId, text, initiator) => (
      if(text.size>MaxMessageLength)
        Future.successful(Failure(new IllegalArgumentException("Message is too long")))
      else
        for {
          post <- repository.getById(postId)
          update <- post match {
            case Some(item) =>
              if(item.user==initiator.userId)
                for {
                  update <- repository.updateMessage(postId, text)
                  read <- repository.getById(postId).map {
                    case Some(x) => Success(x)
                    case None => Failure(new IllegalArgumentException("Not found"))
                  }
                } yield read
              else
                Future.successful(Failure(new AccessDeniedException(s"Not enough permissions to change post state")))
            case None => Future.successful(Failure(new IllegalArgumentException(s"Post id='$postId' not found")))
          }
        } yield update
      ) pipeTo sender
    case post:Post => (
      if(post.text.size>MaxMessageLength)
        Future.successful(Failure(new IllegalArgumentException("Message is too long")))
      else
        for {
          insert <- repository.insertRecord(post)
          read <- repository.getById(post.id)
        } yield read.map(item=>Success(item)).getOrElse(Failure(new IllegalStateException("Post not saved")))
      ) pipeTo sender
    case SetActive(postId, active, initiator) => (
        for {
          post <- repository.getById(postId)
          update <- post match {
            case Some(item) =>
              if(item.user==initiator.userId || initiator.roles.contains(Role.Admin))
                for {
                  setActive <- repository.setActive(postId, active)
                  read <- repository.getById(postId).map {
                    case Some(x) => Success(x)
                    case None => Failure(new IllegalArgumentException("Not found"))
                  }
                } yield read
              else
                Future.successful(Failure(new AccessDeniedException(s"Not enough permissions to change post state")))
            case None => Future.successful(Failure(new IllegalArgumentException(s"Post id='$postId' not found")))
          }
        } yield update
      ) pipeTo sender
  }
}

object WritePostActor {
  def props(repository:PostRepository, MaxMessageLength: Int) = Props(
    classOf[WritePostActor],
    repository, MaxMessageLength
  )
}
