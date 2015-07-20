package controllers

import com.eny.service.AccessDeniedException
import com.eny.service.Role.Role
import play.api.mvc.{AnyContent, Request}
import play.api.mvc.RequestHeader
import securesocial.core.{RuntimeEnvironment, SecureSocial}
import service.MultiProfileUser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}

trait UserContext {
  def current(implicit request:RequestHeader, env:RuntimeEnvironment[MultiProfileUser]): Future[Option[MultiProfileUser]]
}

abstract class SecuredExecution(context:UserContext = new UserContext {
  override def current(implicit request : RequestHeader, env:RuntimeEnvironment[MultiProfileUser]): Future[Option[MultiProfileUser]] = SecureSocial.currentUser[MultiProfileUser]
}) extends SecureSocial[MultiProfileUser] {
  def securedAjaxWithUser[T]
    (role:Role)
    (target: MultiProfileUser => Future[Try[T]])
    (implicit request: Request[AnyContent]) = {
      for (
        user <- context.current;
        action <- user match {
          case Some(usr) =>
            if(usr.roles.contains(role)) target(usr)
            else Future.successful(Failure(new AccessDeniedException("Access denied")))
          case None => Future.successful(Failure(new AccessDeniedException("Not authorized")))
        }
      ) yield action
  }
}
