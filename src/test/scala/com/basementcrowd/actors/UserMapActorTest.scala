package com.basementcrowd.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import com.basementcrowd.actors.UserActor.{Message, MsgResult}
import com.basementcrowd.model.{Address, Organisation, User}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

import scala.reflect.ClassTag

class UserMapActorTest(_system: ActorSystem) extends TestKit(_system) with FunSuiteLike with BeforeAndAfterAll with MockitoSugar {
  def this() = this(ActorSystem("UserActorTest"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  val defOrgMap = Map("1" -> mockOrg("1", "1"))
  val defAddrMap = Map("1" -> mockAddress("1"))

  def fixture(users: Map[String, User]) = new {
    val probe = TestProbe()
    val userActor = system.actorOf(UserMapActor.props(users, defOrgMap, defAddrMap))

    def get(id: String) = tell[Option[User]](userActor, probe)(UserActor.GetUser(id))

    def create(user: User) = tell[MsgResult](userActor, probe)(UserActor.CreateUser(user))

    def update(id: String, user: User) = tell[MsgResult](userActor, probe)(UserActor.UpdateUser(id, user))

    def delete(id: String) = tell[MsgResult](userActor, probe)(UserActor.DeleteUser(id))

    private def tell[A](actor: ActorRef, probe: TestProbe)(msg: Any)(implicit a: ClassTag[A]): A = {
      actor.tell(msg, probe.ref)
      probe.expectMsgType[A]
    }
  }

  def mockAddress(aid: String): Address = Address(aid, "", "", "", "")

  def mockOrg(oid: String, aid: String): Organisation = Organisation(oid, "", "", "", mockAddress(aid))

  def mockUser(): User = mockUser("1", "1", "1")

  def mockUser(uid: String, oid: String, aid: String): User = User(uid, mockOrg(oid, aid), mockAddress(aid), "", "", "", "", "", "")

  test("get user returns user if exists") {
    val users = Map("0" -> mock[User], "1" -> mock[User], "2" -> mock[User])
    val f = fixture(users)
    assert(f.get("1") == Some(users("1")))
  }

  test("get users returns None if doesn't exist") {
    val f = fixture(Map("0" -> mock[User], "3" -> mock[User], "2" -> mock[User]))
    assert(f.get("1") == None)
  }

  test("create user returns user created if user wasn't there before") {
    val f = fixture(Map.empty)
    assert(f.create(mockUser) == Right(Message("User created.")))
  }

  test("create user returns error if user already there") {
    val user = mockUser
    val f = fixture(Map("1" -> user))
    assert(f.create(user) == Left(Message("Cannot create User as User with that ID already exists.")))
  }

  test("create user returns error if organisation is missing") {
    val user = mockUser("1", "2", "1")
    val f = fixture(Map.empty)
    assert(f.create(user) == Left(Message("Organisation ID doesn't match existing Organisation.")))
  }

  test("create user returns error if address is missing") {
    val user = mockUser("1", "1", "2")
    val f = fixture(Map.empty)
    assert(f.create(user) == Left(Message("Address ID doesn't match existing Address.")))
  }

  test("create user adds user to actors map") {
    val user = mockUser
    val f = fixture(Map.empty)
    f.create(user)
    assert(f.get("1") == Some(user))
  }

  test("update user returns error if user isn't there") {
    val f = fixture(Map.empty)
    assert(f.update("1", mock[User]) == Left(Message("User not found.")))
  }

  test("update user returns user updated if user there") {
    val user = mockUser
    val f = fixture(Map("1" -> user))
    assert(f.update("1", user) == Right(Message("User updated.")))
  }

  test("cannot update user to id of another user") {
    val oldUser = mockUser
    val blockingUser = mockUser("2", "1", "1")
    val f = fixture(Map("1" -> oldUser, "2" -> blockingUser))
    assert(f.update("1", blockingUser) == Left(Message("New User ID clashes with another user.")))
  }

  test("update user updates user in actor") {
    val oldUser = mockUser
    val newUser = mockUser
    val f = fixture(Map("1" -> oldUser))
    f.update("1", newUser)
    assert(f.get("1") == Some(newUser))
  }

  test("update user can update users id") {
    val oldUser = mockUser
    val newUser = mockUser("2", "1", "1")
    val f = fixture(Map("1" -> oldUser))
    f.update("1", newUser)
    assert(f.get("2") == Some(newUser))
  }

  test("delete user returns user deleted if user was there") {
    val f = fixture(Map("1" -> mock[User]))
    assert(f.delete("1") == Right(Message("User deleted.")))
  }

  test("delete user returns error if user wasn't there") {
    val f = fixture(Map("1" -> mock[User]))
    assert(f.delete("2") == Left(Message("User not found.")))
  }

  test("delete user removes user from actor") {
    val f = fixture(Map("1" -> mock[User]))
    f.delete("1")
    assert(f.get("1") == None)
  }

}
