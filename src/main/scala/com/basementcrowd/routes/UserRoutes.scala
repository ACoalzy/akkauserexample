package com.basementcrowd.routes

import scala.concurrent.duration._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.pattern.ask
import com.basementcrowd.actors.UserActor
import com.basementcrowd.actors.UserActor.MsgResult
import com.basementcrowd.model.User
import com.basementcrowd.utils.JsonSupport

import scala.concurrent.Future

trait UserRoutes extends JsonSupport {

  implicit def system: ActorSystem

  def userActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val route: Route =
    pathPrefix("user") {
      concat(
        path(Segment) {
          userIdRoute(_)
        },
        post {
          entity(as[User]) { user =>
            val userUpdated: Future[MsgResult] = (userActor ? UserActor.CreateUser(user)).mapTo[MsgResult]
            onSuccess(userUpdated) {
              chooseStatus(StatusCodes.Conflict, StatusCodes.Created, _)
            }
          }
        }
      )
    }

  def userIdRoute(id: String): Route = {
    concat(
      get {
        val user: Future[Option[User]] = (userActor ? UserActor.GetUser(id)).mapTo[Option[User]]
        onSuccess(user) {
          case Some(user) => complete(user)
          case None => complete((StatusCodes.NotFound, UserActor.Message("User not found")))
        }
      },
      put {
        entity(as[User]) { user =>
          val userUpdated: Future[MsgResult] = (userActor ? UserActor.UpdateUser(id, user)).mapTo[MsgResult]
          onSuccess(userUpdated) {
            chooseStatus(StatusCodes.Conflict, StatusCodes.Created, _)
          }
        }
      },
      delete {
        val userDeleted: Future[MsgResult] = (userActor ? UserActor.DeleteUser(id)).mapTo[MsgResult]
        onSuccess(userDeleted) {
          chooseStatus(StatusCodes.NotFound, StatusCodes.OK, _)
        }
      }
    )
  }

  private def chooseStatus(lStatus: StatusCode, rStatus: StatusCode, choice: MsgResult): Route = choice match {
    case Left(m) => complete((lStatus, m))
    case Right(m) => complete((rStatus, m))
  }
}
