package com.basementcrowd.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import com.basementcrowd.actors.UserActor.{Message, MsgResult}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import org.mockito.Mockito.when

import scala.reflect.ClassTag

class UserActorTest(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with BeforeAndAfterAll with MockitoSugar {
  def this() = this(ActorSystem("UserActorTest"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  def fixture(users: Map[String, User]) = new {
    val probe = TestProbe()
    val userActor = system.actorOf(UserActor.props(users))
    def get(id: String) = tell[Option[User]](userActor, probe)(UserActor.GetUser(id))
    def create(user: User) = tell[MsgResult](userActor, probe)(UserActor.CreateUser(user))
    def update(id: String, user: User) = tell[MsgResult](userActor, probe)(UserActor.UpdateUser(id, user))
    def delete(id: String) = tell[MsgResult](userActor, probe)(UserActor.DeleteUser(id))

    private def tell[A](actor: ActorRef, probe: TestProbe)(msg: Any)(implicit a: ClassTag[A]): A = {
      actor.tell(msg, probe.ref)
      probe.expectMsgType[A]
    }
  }

  def mockUser(id: String): User = {
    val user = mock[User]
    when(user.id).thenReturn(id)
    return user
  }

  test("get user returns user if exists") {
    val users = Map("0" -> mock[User], "1" -> mock[User], "2" -> mock[User])
    val f = fixture(users)
    assert(f.get("1") == Some(users("1")))
  }

  test("get users returns None if doesn't exist") {
    val f = fixture(Map("0" -> mock[User], "3" -> mock[User], "2" -> mock[User]))
    assert(f.get("1") == None)
  }

  test("create user returns true if user wasn't there before") {
    val f = fixture(Map.empty)
    assert(f.create(mock[User]) == Right(Message("User created.")))
  }

  test("create user returns false if user already there") {
    val user = mockUser("1")
    val f = fixture(Map("1" -> user))
    assert(f.create(user) == Left(Message("Cannot create User as User with that ID already exists.")))
  }

  test("create user adds user to actors map") {
    val user = mockUser("1")
    val f = fixture(Map.empty)
    f.create(user)
    assert(f.get("1") == Some(user))
  }

  test("update user returns false if user isn't there") {
    val f = fixture(Map.empty)
    assert(f.update("1", mock[User]) == Left(Message("User not found.")))
  }

  test("update user returns true if user there") {
    val user = mockUser("1")
    val f = fixture(Map("1" -> user))
    assert(f.update("1", user) == Right(Message("User updated.")))
  }

  test("update user updates user in actor") {
    val oldUser = mockUser("1")
    val newUser = mockUser("1")
    val f = fixture(Map("1" -> oldUser))
    f.update("1", newUser)
    assert(f.get("1") == Some(newUser))
  }

  test("update user can update users id") {
    val oldUser = mockUser("1")
    val newUser = mockUser("2")
    val f = fixture(Map("1" -> oldUser))
    f.update("1", newUser)
    assert(f.get("2") == Some(newUser))
  }

  test("cannot update user to id of another user") {
    val oldUser = mockUser("1")
    val blockingUser = mockUser("2")
    val f = fixture(Map("1" -> oldUser, "2" -> blockingUser))
    assert(f.update("1", blockingUser) == Left(Message("New User ID clashes with another user.")))
  }

  test("delete user returns true if user was there") {
    val f = fixture(Map("1" -> mock[User]))
    assert(f.delete("1") == Right(Message("User deleted.")))
  }

  test("delete user returns false if user wasn't there") {
    val f = fixture(Map("1" -> mock[User]))
    assert(f.delete("2") == Left(Message("User not found.")))
  }

  test("delete user removes user from actor") {
    val f = fixture(Map("1" -> mock[User]))
    f.delete("1")
    assert(f.get("1") == None)
  }

}
