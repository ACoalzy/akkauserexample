package com.basementcrowd

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.basementcrowd.actors.UserActor
import com.basementcrowd.routes.UserRoutes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Server extends App with UserRoutes {
  implicit val system = ActorSystem("basementcrowd-test")
  implicit val materializer = ActorMaterializer()

  val userActor = system.actorOf(UserActor.props(Map.empty))

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
