package com.eny.service


import com.eny.model._
import com.eny.model.bean.Post

import scala.concurrent.Future
import scala.util.Try

trait PostsService {

  def list(limit: Int, start: Option[PostID]):Future[List[Post]]

  def create(post:Post):Future[Try[Post]]

  def update(postId:PostID, message:String, initiator:Initiator):Future[Try[Post]]

  def setActive(postId:PostID, active:Boolean, initiator:Initiator):Future[Try[Post]]
}
