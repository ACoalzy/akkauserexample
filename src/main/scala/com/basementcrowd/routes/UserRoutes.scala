package com.basementcrowd.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.basementcrowd.actors.UserHandler
import com.basementcrowd.actors.UserHandler.MsgResult
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
          entity(as[User]) { user =>
            val userUpdated: Future[MsgResult] = (userActor ? UserHandler.CreateUser(user)).mapTo[MsgResult]
            chooseStatus(StatusCodes.Conflict, StatusCodes.Created, userUpdated)
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
        entity(as[User]) { user =>
          val userUpdated: Future[MsgResult] = (userActor ? UserHandler.UpdateUser(id, user)).mapTo[MsgResult]
          chooseStatus(StatusCodes.Conflict, StatusCodes.Created, userUpdated)
        }
      },
      delete {
        val userDeleted: Future[MsgResult] = (userActor ? UserHandler.DeleteUser(id)).mapTo[MsgResult]
        chooseStatus(StatusCodes.NotFound, StatusCodes.OK, userDeleted)
      }
    )
  }

  /**
    * Choose left or right status based on future choice being a Left or Right
    *
    * @param lStatus
    * @param rStatus
    * @param choice
    * @return
    */
  private def chooseStatus(lStatus: StatusCode, rStatus: StatusCode, choice: Future[MsgResult]): Route =
    onSuccess(choice) {
      _ match {
        case Left(m) => complete((lStatus, m))
        case Right(m) => complete((rStatus, m))
      }
    }
}
