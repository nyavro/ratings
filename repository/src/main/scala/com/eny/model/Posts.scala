package com.eny.model

import java.util.UUID

import com.eny.model.bean.Post
import com.eny.repository.PostRepository

import scala.concurrent.Future

object Posts extends PostRepository {

  def asMap(list: List[Post]) = list.map(item => item.id -> item).toMap

  var posts = asMap(
    List(
      Post(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "first"),
      Post(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "second"),
      Post(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "third")
    )
  )

  def insertRecord(post: Post) = {
    posts = posts + (post.id -> post)
    Future.successful(true)
  }

  def getById(postId:PostID) = Future.successful(posts.get(postId))

  def updateMessage(postId: PostID, message: String) =
    Future.successful(
      posts.get(postId) match {
        case Some(post) =>
          posts = posts - postId
          posts = posts + (postId -> post.copy(text = message))
          true
        case None => false
      }
    )

  def setActive(postId:PostID, active: Boolean) =
    Future.successful(
      posts.get(postId) match {
        case Some(post) =>
          posts = posts - postId
          posts = posts + (postId -> post.copy(active = active))
          true
        case None => false
      }
    )

  def getByUser(userId:UserID) =
    Future.successful(
      posts
        .filter {
          case (id, post) => post.user==userId
        }
        .values
        .toList
    )

  def list(limit:Int, from:Option[PostID] = None) =
    Future.successful(
      posts
        .dropWhile {
          case (id, post) => !id.equals(from.getOrElse(id))
        }
        .take(limit)
        .values
        .toList
    )
}