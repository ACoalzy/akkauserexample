package com.basementcrowd.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.FunSuite
import akka.testkit.TestProbe
import com.basementcrowd.actors.UserActor
import com.basementcrowd.actors.UserActor.Message
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
    setupProbe(UserActor.GetUser("123"), Some(dummyUser))
    checkUser(request, StatusCodes.OK, dummyUser)
  }

  test("GET /user/id handles no result") {
    val request = Get(uri = "/user/123")
    setupProbe(UserActor.GetUser("123"), None)
    checkMsg(request, StatusCodes.NotFound, Message("User not found"))
  }

  test("POST /user handles success") {
    val request = Post(uri = "/user").withEntity(dummyEntity)
    setupProbe(UserActor.CreateUser(dummyUser), Right(dummyMsg))
    checkMsg(request, StatusCodes.OK, dummyMsg)
  }

  test("POST /user handles id already present") {
    val request = Post(uri = "/user").withEntity(dummyEntity)
    setupProbe(UserActor.CreateUser(dummyUser), Left(dummyMsg))
    checkMsg(request, StatusCodes.Conflict, dummyMsg)
  }

  test("PUT /user/id handles success") {
    val request = Put(uri = "/user/123").withEntity(dummyEntity)
    setupProbe(UserActor.UpdateUser("123", dummyUser), Right(dummyMsg))
    checkMsg(request, StatusCodes.OK, dummyMsg)
  }

  test("PUT /user/id handles failure") {
    val request = Put(uri = "/user/123").withEntity(dummyEntity)
    setupProbe(UserActor.UpdateUser("123", dummyUser), Left(dummyMsg))
    checkMsg(request, StatusCodes.Conflict, dummyMsg)
  }

  test("DELETE /user/id handles success") {
    val request = Delete(uri = "/user/123")
    setupProbe(UserActor.DeleteUser("123"), Right(dummyMsg))
    checkMsg(request, StatusCodes.OK, dummyMsg)
  }

  test("DELETE /user/id handles id not being found") {
    val request = Delete(uri = "/user/123")
    setupProbe(UserActor.DeleteUser("123"), Left(dummyMsg))
    checkMsg(request, StatusCodes.NotFound, dummyMsg)
  }
}
