package controllers

import java.io.{File, FileInputStream}
import java.util.UUID
import javax.ws.rs.QueryParam

import akka.actor.ActorSystem
import com.eny.model.UserID
import com.eny.service.Role
import com.eny.service.impl.ActorsBasedUsersService
import com.wordnik.swagger.annotations.{ApiOperation, _}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._
import securesocial.core.RuntimeEnvironment
import service.MultiProfileUser

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = "/users", description = "Operations about users")
class UsersController(
    override implicit val env: RuntimeEnvironment[MultiProfileUser],
    system:ActorSystem
  ) extends SecuredExecution with ResponseFormatter {

  val service = new ActorsBasedUsersService(system)
  val AboutMeMaxLength = 140

  @ApiOperation(nickname = "uploadAvatar",
    value = "Attach an Image File for a user",
    notes = "Upload image file",
    response = classOf[Void], httpMethod = "POST")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid file format")))
  def uploadAvatar = Action(parse.multipartFormData) { implicit request =>
    implicit val request1: Request[AnyContent] = request.asInstanceOf[Request[AnyContent]]
    securedAjaxWithUser(Role.User) {
      user => request.body.file("file").map {
        avatar =>
          avatar.ref.moveTo(new File(avatar.filename))
          service.uploadAvatar(UUID.randomUUID(), new FileInputStream(avatar.filename))
      }.getOrElse(Future.successful(Failure(new IllegalStateException(""))))
    }.map(
      item => Ok(convertToJson(item))
    )
    Ok("File has been uploaded")
  }

  @ApiOperation(
    nickname = "aboutMe",
    value = "Set about me information to user",
    response = classOf[Void], httpMethod = "PUT"
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 400, message = "Invalid text")
    )
  )
  def aboutMe(
      @ApiParam(value = "About me text", required = true)
      @QueryParam("text")
      text:String) = Action.async { implicit request: Request[AnyContent] =>
    securedAjaxWithUser(Role.User) {
      user => 
		if(text.length>AboutMeMaxLength)
      Future.successful(Failure(new IllegalArgumentException(s"About me text length exceeds maximum=$AboutMeMaxLength")))
    else
      service.setAboutMe(user.userId, text)
    }.map {
      case res => Ok(convertToJson(res))
    }
  }
}

