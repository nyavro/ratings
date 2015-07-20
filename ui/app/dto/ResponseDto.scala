package dto

import java.util.Date

import com.eny.model._

case class ResponseDto(
  text: String,
  post: String,
  date: Option[Date] = Some(new Date),
  active: Option[Boolean] = Some(true),
  user: Option[String] = None,
  id: Option[String] = None
)
