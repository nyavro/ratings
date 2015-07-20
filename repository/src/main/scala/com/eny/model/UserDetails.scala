package com.eny.model

import java.util.Date

import scala.concurrent.Future
import scala.util.Success


case class UserDetails(
  id: UserDetailsID,
  gender: Option[String],
  birthDate: Option[Date],
  city: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarUrl: Option[String],
  roles: Set[String],
  banned: Boolean,
  aboutme: Option[String]
)


object UserDetailsTable {

  var list = List[UserDetails]()

  def insertRecord(details: UserDetails) = {
    list = details::list
    Future.successful(true)
  }

  def findById(id: UserDetailsID) = Future.successful(list.find(item => item.id==id))

  def updateAvatar(id: UserDetailsID, path: String) = Future.successful(Success(true))

  def addRole(id: UserDetailsID, role:String) = Future.successful(true)

  def findByIds(ids: List[UserDetailsID]) = Future.successful(list.filter(item => ids.contains(item.id)))

  def ban(id: UserDetailsID, value:Boolean) = Future.successful(true)

  def setAboutme(id: UserDetailsID, aboutme:Option[String]) = Future.successful(true)
}




