package CuriosityWeb.service

import CuriosityWeb.model.JsonFormat._
import CuriosityWeb.model.User
import CuriosityWeb.repo.UserRepo
import akka.Done
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Sink, Source}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType
import spray.json.DefaultJsonProtocol._

import java.sql.SQLIntegrityConstraintViolationException
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

@Tag(name = "UserService")
@Path("/user")
final class UserService(userRepo: UserRepo)(implicit system: ActorSystem[_], ec: ExecutionContext) extends Service {

  import log._

  override def routes: Route = pathPrefix("user") {
    addUser ~
      addUsers ~
      getUser ~
      getAllUsers ~
      getAllUserIds ~
      removeUser ~
      removeUsers ~
      updateName ~
      updateMobile ~
      removeMobile
  }

  @POST
  @Path("/addUser")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Add user into the database")
  @RequestBody(required = true, content = Array(new Content(mediaType = MediaType.APPLICATION_JSON, schema = new Schema(implementation = classOf[User]))))
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  @ApiResponse(responseCode = "409", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def addUser: Route = path("addUser") {
    post {
      entity(as[User]) { user =>
        onComplete(userRepo.addUser(user)) {
          case Success(_) => complete(StatusCodes.OK, "User added")
          case Failure(_: SQLIntegrityConstraintViolationException) => complete(StatusCodes.Conflict, s"User already exists for id: ${user.id}")
          case Failure(exception) =>
            error(s"Failed to add user $user", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  @POST
  @Path("addUsers")
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Add multiple users into the database")
  @RequestBody(required = true, content = Array(new Content(mediaType = MediaType.APPLICATION_JSON, array = new ArraySchema(schema = new Schema(implementation = classOf[User])))))
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.APPLICATION_JSON)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def addUsers: Route = path("addUsers") {
    post {
      entity(as[Set[User]]) { users =>
        onComplete(Source(users)
          .mapAsync(10) { user =>
            userRepo.addUser(user).transform(res => Success(user.id -> res))
          }.runWith(Sink.collection[(String, Try[Done]), Map[String, Try[Done]]])) {
          case Success(value) =>
            complete(value.view.mapValues {
              case Success(_) => "User added"
              case Failure(_: SQLIntegrityConstraintViolationException) => s"User already exists"
              case Failure(exception) => exception.getMessage
            }.toMap[String, String])
          case Failure(exception) =>
            error(s"Failed to add users $users", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  @GET
  @Path("/getUser")
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN))
  @Operation(summary = "Query user based on id")
  @Parameter(name = "id", required = true, in = ParameterIn.QUERY, example = "string")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.APPLICATION_JSON, schema = new Schema(implementation = classOf[User]))))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def getUser: Route = path("getUser") {
    parameter("id".as[String]) { id =>
      onComplete(userRepo.getUser(id)) {
        case Success(Some(user)) => complete(user)
        case Success(None) => complete(StatusCodes.NotFound, s"User with id $id not found")
        case Failure(exception) =>
          error(s"Failed to fetch user for id $id")
          complete(StatusCodes.InternalServerError, exception.getMessage)
      }
    }
  }

  @GET
  @Path("/getAllUsers")
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN))
  @Operation(summary = "Get all users from database")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.APPLICATION_JSON, array = new ArraySchema(schema = new Schema(implementation = classOf[User])))))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def getAllUsers: Route = path("getAllUsers") {
    onComplete(userRepo.getAllUsers) {
      case Success(users) => complete(users)
      case Failure(exception) =>
        error("Failed to fetch all users", exception)
        complete(StatusCodes.InternalServerError, exception.getMessage)
    }
  }

  @GET
  @Path("/getAllUserIds")
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN))
  @Operation(summary = "Get all user ids from database")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.APPLICATION_JSON, array = new ArraySchema(schema = new Schema(implementation = classOf[String])))))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def getAllUserIds: Route = path("getAllUserIds") {
    onComplete(userRepo.getAllUserIds) {
      case Success(userIds) => complete(userIds)
      case Failure(exception) =>
        error("Failed to fetch all users", exception)
        complete(StatusCodes.InternalServerError, exception.getMessage)
    }
  }

  @DELETE
  @Path("/removeUser")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Operation(summary = "Remove user from database")
  @Parameter(name = "id", required = true, in = ParameterIn.QUERY, example = "string")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def removeUser: Route = path("removeUser") {
    delete {
      parameter("id".as[String]) { id =>
        onComplete(userRepo.removeUser(id)) {
          case Success(_) => complete(StatusCodes.OK, "User Removed")
          case Failure(exception) =>
            error(s"Failed to remove user with id: $id", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  @DELETE
  @Path("/removeUsers")
  @Produces(Array(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Remove multiple users from database")
  @RequestBody(required = true, content = Array(new Content(mediaType = MediaType.APPLICATION_JSON, array = new ArraySchema(schema = new Schema(implementation = classOf[String])))))
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.APPLICATION_JSON)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def removeUsers: Route = path("removeUsers") {
    delete {
      entity(as[Set[String]]) { ids =>
        onComplete(Source(ids).
          mapAsync(10) { id =>
            userRepo.removeUser(id).transform(res => Success(id -> res))
          }.runWith(Sink.collection[(String, Try[Done]), Map[String, Try[Done]]])) {
          case Success(idsWithRes) =>
            complete(idsWithRes.view.mapValues {
              case Success(_) => "User removed"
              case Failure(exception) => exception.getMessage
            }.toMap[String, String])
          case Failure(exception) =>
            error(s"Failed to remove users with ids: $ids", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  @PUT
  @Path("/updateName")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Parameter(name = "id", required = true, in = ParameterIn.QUERY, example = "string")
  @Parameter(name = "name", required = true, in = ParameterIn.QUERY, example = "string222")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def updateName: Route = path("updateName") {
    put {
      parameter("id".as[String], "name".as[String]) { (id, name) =>
        onComplete(userRepo.updateName(id, name)) {
          case Success(_) => complete(StatusCodes.OK, "Name Updated")
          case Failure(exception) =>
            error(s"Failed to update name: $name for id: $id", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  @PUT
  @Path("/updateMobile")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Parameter(name = "id", required = true, in = ParameterIn.QUERY, example = "string")
  @Parameter(name = "mobile", required = true, in = ParameterIn.QUERY, example = "string222")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def updateMobile: Route = path("updateMobile") {
    put {
      parameter("id".as[String], "mobile".as[String]) { (id, mobile) =>
        onComplete(userRepo.updateMobile(id, mobile)) {
          case Success(_) => complete(StatusCodes.OK, "Mobile Number Updated")
          case Failure(exception) =>
            error(s"Failed to update mobile: $mobile for id: $id", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  @PUT
  @Path("/removeMobile")
  @Produces(Array(MediaType.TEXT_PLAIN))
  @Parameter(name = "id", required = true, in = ParameterIn.QUERY, example = "string")
  @ApiResponse(responseCode = "200", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  @ApiResponse(responseCode = "500", content = Array(new Content(mediaType = MediaType.TEXT_PLAIN)))
  def removeMobile: Route = path("removeMobile") {
    put {
      parameter("id".as[String]) { id =>
        onComplete(userRepo.removeMobile(id)) {
          case Success(_) => complete(StatusCodes.OK, "Mobile Number Removed")
          case Failure(exception) =>
            error(s"Failed to remove mobile number for id: $id", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }
}
