package com.eny.transport

import akka.actor.ActorRef
import com.eny.service.Initiator

trait Action {
  def actor:ActorRef
}

case class Message[T](value:T, initiator: Initiator, target:ActorRef)
case class Command[T](action:Action, message:Message[T])