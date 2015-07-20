package dto

import java.util.Date

import com.eny.model._

case class UserDto (
  id: UserID,
  gender: Option[String],
  birthDate: Option[Date],
  city: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarUrl: Option[String]
)
