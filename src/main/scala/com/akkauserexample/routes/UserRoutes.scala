package com.akkauserexample.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.akkauserexample.actors.UserHandler._
import com.akkauserexample.model.User
import com.akkauserexample.utils._

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
            user => processBasicCRUDRequest(CreateUser(user))
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
        val user: Future[Option[User]] = (userActor ? GetUser(id)).mapTo[Option[User]]
        onSuccess(user) {
          case Some(user) => complete(user)
          case None => complete((StatusCodes.NotFound, Message("User not found")))
        }
      },
      put {
        entity(as[User]) {
          user => processBasicCRUDRequest(UpdateUser(id, user))
        }
      },
      delete {
        processBasicCRUDRequest(DeleteUser(id))
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
      case r => complete((ResponseCodes.responseToHTTP(r.responseCode), r.message))
    }
  }
}
