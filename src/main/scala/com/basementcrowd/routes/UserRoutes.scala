package com.basementcrowd.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.basementcrowd.actors.UserHandler
import com.basementcrowd.actors.UserHandler.{CRUD, Response}
import com.basementcrowd.model.User
import com.basementcrowd.utils.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._

trait UserRoutes extends JsonSupport {

  implicit def system: ActorSystem

  def userActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  // route for all things beginning user
  lazy val route: Route =
    pathPrefix("user") {
      concat(
        path(Segment) {
          userIdRoute(_)
        },
        post {
          entity(as[User]) {
            user => processBasicCRUDRequest(UserHandler.CreateUser(user))
          }
        }
      )
    }

  /**
    * Route for end point /user/id
    *
    * @param id
    * @return
    */
  def userIdRoute(id: String): Route = {
    concat(
      get {
        val user: Future[Option[User]] = (userActor ? UserHandler.GetUser(id)).mapTo[Option[User]]
        onSuccess(user) {
          case Some(user) => complete(user)
          case None => complete((StatusCodes.NotFound, UserHandler.Message("User not found")))
        }
      },
      put {
        entity(as[User]) {
          user => processBasicCRUDRequest(UserHandler.UpdateUser(id, user))
        }
      },
      delete {
        processBasicCRUDRequest(UserHandler.DeleteUser(id))
      }
    )
  }

  /**
    * Send CRUD request to actor, wait for response and separate into tuple for json parsing
    * @param crud
    * @return
    */
  private def processBasicCRUDRequest(crud: CRUD): Route = {
    val response: Future[Response] = (userActor ? crud).mapTo[Response]
    onSuccess(response) {
      case r => complete((r.statusCode, r.message))
    }
  }
}
