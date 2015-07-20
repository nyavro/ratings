package dto

import java.util.Date

import com.eny.model._

case class PostDto (
  text: String,
  category: CategoryID,
  date: Option[Date] = Some(new Date),
  active: Option[Boolean] = Some(true),
  id: Option[PostID] = None,
  user: Option[UserID] = None
)
