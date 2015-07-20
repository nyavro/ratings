package com.eny.repository

import com.eny.model._
import com.eny.model.bean.Post

import scala.concurrent.Future

trait PostRepository extends Repository[Post]{

  def insertRecord(post: Post): Future[Boolean]

  def getById(id:PostID): Future[Option[Post]]

  def updateMessage(postId: PostID, message: String): Future[Boolean]

  def setActive(postId:PostID, active: Boolean): Future[Boolean]

  def getByUser(userId:UserID): Future[Seq[Post]]


  def list(limit:Int, from:Option[PostID] = None): Future[List[Post]]
}
