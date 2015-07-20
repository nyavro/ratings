package com.eny.model.bean

import java.util.Date

import com.eny.model._

case class Post (
  id: PostID,
  user: UserID,
  category: CategoryID,
  text: String,
  date: Date = new Date,
  active: Boolean = true
)