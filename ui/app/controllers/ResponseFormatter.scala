package controllers

import com.eny.model._
import com.eny.model.bean.Post
import dto.{CategoryDto, PostDto, ResponseDto, UserDto}
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

trait ResponseFormatter {
  implicit val userWrites = Json.writes[UserDto]
  implicit val postWrites = Json.writes[Post]
  implicit val postDtoReades = Json.reads[PostDto]
  implicit val responseDtoReads = Json.reads[ResponseDto]
  implicit val categoryDtoReads = Json.reads[CategoryDto]

  def convertToJson[T](either: Try[T])(implicit writes:Writes[T]) =
    either match {
      case Success(value) =>
        JsObject(
          Seq(
            ("message", JsString("succeeded")),
            ("code", JsString("0")),
            ("result", Json.toJson(value))
          )
        )
      case Failure(err) =>
        JsObject(
          Seq(
            ("message", JsString("failed")),
            ("code", JsString("-1")),
            ("result", Json.toJson(err.getLocalizedMessage))
          )
        )
    }

  def toDto(user:UserDetails): UserDto = UserDto(
    user.id, user.gender, user.birthDate, user.city,
    user.firstName, user.lastName, user.fullName, user.email, user.avatarUrl
  )
}