package controllers

import java.util.UUID
import javax.ws.rs.QueryParam

import akka.actor.ActorSystem
import com.eaio.uuid.{UUID => TimeUUID}
import com.eny.model.bean.Post
import com.eny.service.impl.ActorsBasedPostsService
import com.eny.service.{Initiator, Role}
import com.wordnik.swagger.annotations._
import configuration.ApplicationContext
import dto.PostDto
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Request}
import securesocial.core.{RuntimeEnvironment, SecureSocial}
import service.MultiProfileUser

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = "/posts", description = "Operations about posts")
class PostsController(
    override implicit val env: RuntimeEnvironment[MultiProfileUser],
    system:ActorSystem,
    context:ApplicationContext
  ) extends SecuredExecution with SecureSocial[MultiProfileUser] with ResponseFormatter {

  lazy val service = new ActorsBasedPostsService(system, 200)

  @ApiOperation(
    nickname = "create",
    value = "Add a new Post",
    response = classOf[Void],
    httpMethod = "POST",
    authorizations = Array(
      new Authorization(
        value="oauth2",
        scopes = Array(
          new AuthorizationScope(scope = "test:anything", description = "anything"),
          new AuthorizationScope(scope = "test:nothing", description = "nothing")
        )
      )
    )
  )
  @ApiResponses(Array(new ApiResponse(code = 405, message = "Invalid input")))
  @ApiImplicitParams(Array(new ApiImplicitParam(value = "Post object that needs to be added to db", required = true, dataType = "Post", paramType = "body")))
  def create = Action.async { implicit request: Request[AnyContent] =>
    securedAjaxWithUser(Role.User) {
      user => request.body.asJson.map {
        json =>
          Json.fromJson[PostDto](json) match {
            case JsSuccess(dto, _) => service.create(Post(UUID.fromString(new TimeUUID().toString), user.userId, dto.category, dto.text))
            case JsError(seq) => Future.successful(Failure(new IllegalStateException(seq.toString())))
          }
      }.getOrElse(Future.successful(Failure(new IllegalStateException("Json parse failed"))))
    }.map {
      case res => Ok(convertToJson(res))
    }
  }

  @ApiOperation(
    nickname = "list",
    value = "Lists page of Posts",
    response = classOf[String], httpMethod = "GET"
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 400, message = "Invalid")
    )
  )
  def list(
      @ApiParam(value = "Page size", required = true)
      @QueryParam("limit")
      limit: Int,
      @ApiParam(value = "Start index of Post", required = false)
      @QueryParam("start")
      start:Option[String]
    ) = Action.async { implicit request: Request[AnyContent] =>
    securedAjaxWithUser(Role.User) {
      _ =>
        service.list(limit, start.map(UUID.fromString)).map(item => Success(item))
    }.map {
      case res => Ok(convertToJson(res))
    }
  }

  def name() = Action.async { implicit request: Request[AnyContent] =>
    Future.successful(
      Ok(
        context.agent match {
          case Success(agent) => agent.name()
          case Failure(ex) => ex.getMessage
        }
      )
    )
  }

  @ApiOperation(
    nickname = "updatePost",
    value = "Update an existing Post's message",
    response = classOf[Void], httpMethod = "PUT"
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 400, message = "Invalid ID supplied"),
      new ApiResponse(code = 404, message = "Post not found"),
      new ApiResponse(code = 405, message = "Validation exception")
    )
  )
  def update(
      @ApiParam(value = "Post id", required = true)
      @QueryParam("postId")
      postId: String,
      @ApiParam(value = "New message of the post", required = true)
      @QueryParam("message")
      message: String) = Action.async { implicit request: Request[AnyContent] =>
    securedAjaxWithUser(Role.User) {
      user =>
        service.update(UUID.fromString(postId), message, Initiator(user.userId, user.roles))
    }.map {
      case res => Ok(convertToJson(res))
    }
  }

  @ApiOperation(
    nickname = "delete",
    value = "Delete Post by id",
    notes = "Provide UUID of the Post", httpMethod = "DELETE")
  @ApiResponses(
    Array(
      new ApiResponse(code = 400, message = "Invalid ID supplied")
    )
  )
  def delete(
      @ApiParam(value = "Post id", required = true)
      @QueryParam("postId")
      postId: String
    ) = Action.async { implicit request:Request[AnyContent] =>
    securedAjaxWithUser(Role.User) {
      user =>
        val uuid = UUID.fromString(postId)
        service.setActive(uuid, active = false, Initiator(user.userId, user.roles))
    }.map {
      case res => Ok(convertToJson(res))
    }
  }
}

