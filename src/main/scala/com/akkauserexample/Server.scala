package com.akkauserexample

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.akkauserexample.actors.UserMapActor
import com.akkauserexample.model.{Address, Organisation}
import com.akkauserexample.routes.UserRoutes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Server extends App with UserRoutes {
  implicit val system = ActorSystem("akkauserexample")
  implicit val materializer = ActorMaterializer()

  // As no end point to create addresses / organisations create some default ones to allow for user creation
  val addressTable = Map(
    "a1" -> Address("a1", "Apt 30", "3 Example Street", "London", "AB1 2CD"),
    "a2" -> Address("a2", "30 Something Lane", "", "London", "BA4 4RY"),
    "a3" -> Address("a3", "Shiny Street", "", "London", "CD1 2EF"),
    "a4" -> Address("a4", "40 Something Street", "", "London", "GA4 4RY"),
    "a5" -> Address("a5", "30 Something Road", "", "London", "HA4 4RY"))
  val organisationTable = Map(
    "o1" -> Organisation("o1", "Barry Org", "barryorg@barry.com", "chambers", addressTable("a1")),
    "o2" -> Organisation("o2", "Garry Org", "garryorg@garry.com", "chambers", addressTable("a3")))

  val userActor = system.actorOf(UserMapActor.props(Map.empty, organisationTable, addressTable))

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
