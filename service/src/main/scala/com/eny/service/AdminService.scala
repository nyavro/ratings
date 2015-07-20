package com.eny.service

import com.eny.model._
import com.eny.service.Role.Role

import scala.concurrent.Future
import scala.util.Try

trait AdminService {

  def setUserRole(userId:UserID, role:Role, initiator:Initiator):Future[Try[Boolean]]

  def banUser(userId:UserID, value:Boolean, initiator:Initiator):Future[Try[Boolean]]
}
