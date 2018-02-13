package com.basementcrowd.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.FunSuite
import akka.testkit.TestProbe
import com.basementcrowd.actors.UserHandler
import com.basementcrowd.actors.UserHandler.{Message, Response}
import com.basementcrowd.model.{Address, Organisation, User}

import scala.concurrent.Future

class UserRoutesTest extends FunSuite with ScalaFutures with ScalatestRouteTest with UserRoutes {
  val probe = TestProbe()
  override val userActor: ActorRef = probe.ref
  val dummyAddr = Address("", "", "", "", "")
  val dummyOrg = Organisation("", "", "", "", dummyAddr)
  val dummyUser = User("", dummyOrg, dummyAddr, "", "", "", "", "", "")
  val dummyEntity = Marshal(dummyUser).to[MessageEntity].futureValue
  val dummyMsg = Message("dummy")

  private def setupProbe[A, B](in: A, out: B) = Future {
    probe.expectMsg(in)
    probe.reply(out)
  }

  private def checkUser(request: HttpRequest, status: StatusCode, result: User): Unit = {
    request ~> route ~> check {
      assert(status == status)
      assert(entityAs[User] == result)
    }
  }

  private def checkMsg(request: HttpRequest, status: StatusCode, result: Message): Unit = {
    request ~> route ~> check {
      assert(status == status)
      assert(entityAs[Message] == result)
    }
  }

  test("GET /user/id handles success") {
    val request = Get(uri = "/user/123")
    setupProbe(UserHandler.GetUser("123"), Some(dummyUser))
    checkUser(request, StatusCodes.OK, dummyUser)
  }

  test("GET /user/id handles no result") {
    val request = Get(uri = "/user/123")
    setupProbe(UserHandler.GetUser("123"), None)
    checkMsg(request, StatusCodes.NotFound, Message("User not found"))
  }

  test("POST /user passes on status code") {
    val request = Post(uri = "/user").withEntity(dummyEntity)
    setupProbe(UserHandler.CreateUser(dummyUser), Response(dummyMsg,StatusCodes.Accepted))
    checkMsg(request, StatusCodes.Accepted, dummyMsg)
  }

  test("PUT /user/id passes on status code") {
    val request = Put(uri = "/user/123").withEntity(dummyEntity)
    setupProbe(UserHandler.UpdateUser("123", dummyUser), Response(dummyMsg,StatusCodes.Created))
    checkMsg(request, StatusCodes.Created, dummyMsg)
  }

  test("DELETE /user/id passes on status code") {
    val request = Delete(uri = "/user/123")
    setupProbe(UserHandler.DeleteUser("123"), Response(dummyMsg,StatusCodes.OK))
    checkMsg(request, StatusCodes.OK, dummyMsg)
  }

}
