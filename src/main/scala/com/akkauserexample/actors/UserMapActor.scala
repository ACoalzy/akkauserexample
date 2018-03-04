package com.akkauserexample.actors

import akka.actor.{Actor, Props}
import com.akkauserexample.actors.UserHandler._
import com.akkauserexample.model.{Address, Organisation, User}
import com.akkauserexample.utils._

object UserMapActor {
  def props(users: Map[String, User], orgs: Map[String, Organisation], addrs: Map[String, Address]): Props =
    Props(new UserMapActor(users, orgs, addrs))
}

class UserMapActor(initUsers: Map[String, User], initOrgs: Map[String, Organisation], initAddr: Map[String, Address]) extends Actor {

  var users: Map[String, User] = initUsers
  var organisations: Map[String, Organisation] = initOrgs
  var addresses: Map[String, Address] = initAddr

  override def receive: Receive = {
    case GetUser(id) => sender() ! getData(id, users, "User")
    case CreateUser(user) => sender() ! createUser(user)
    case UpdateUser(id, user) => sender() ! updateUser(id, user)
    case DeleteUser(id) => sender() ! deleteUser(id)
  }

  private def getData[A](id: String, dataMap: Map[String, A], name: String): Either[Message, A] = {
    dataMap.get(id) match {
      case Some(d) => Right(d)
      case None => Left(Message(s"$name ID doesn't match existing $name."))
    }
  }

  /**
    * Returns user with updated organisation and address if both present<br>
    * Otherwise returns error message detailing which is missing
    *
    * @param user
    * @return
    */
  private def getOrgAndAddress(user: User): Either[Message, User] = {
    for {
      o <- getData(user.organisation.id, organisations, "Organisation")
      a <- getData(user.address.id, addresses, "Address")
    } yield (user.copy(organisation = o, address = a))
  }

  /**
    * Create a new user, if below are true (note, ignores Organisation and Address fields apart from ID):<br>
    * - User ID doesn't already exist<br>
    * - Organisation ID matches existing Organisation<br>
    * - Address ID matches existing Address
    *
    * @param user
    * @return
    */
  private def createUser(user: User): Response =
    users.get(user.id) match {
      case Some(_) => Response(Message("Cannot create User as User with that ID already exists."), ResponseCodes.Conflict)
      case None => getOrgAndAddress(user) match {
        case Right(user) => {
          users += user.id -> user
          Response(Message("User created."), ResponseCodes.Created)
        }
        case Left(m) => Response(m, ResponseCodes.Conflict)
      }
    }

  /**
    * Update an existing user, if the below are true (note, ignores Organisation and Address fields apart from ID):<br>
    * - User ID already exists<br>
    * - New user ID (if different) doesn't already exist<br>
    * - Organisation ID matches existing Organisation<br>
    * - Address ID matches existing Address
    *
    * @param id
    * @param user
    * @return
    */
  private def updateUser(id: String, user: User): Response = {
    val toUpdate = users.get(id)
    val updateTo = if (id != user.id) users.get(user.id) else None
    (toUpdate, updateTo) match {
      case (None, _) => Response(Message("User not found."), ResponseCodes.Missing)
      case (_, Some(_)) => Response(Message("New User ID clashes with another user."), ResponseCodes.Conflict)
      case _ => getOrgAndAddress(user) match {
        case Right(user) => {
          users -= id
          users += user.id -> user
          Response(Message("User updated."), ResponseCodes.OK)
        }
        case Left(m) => Response(m, ResponseCodes.Conflict)
      }
    }
  }

  /**
    * Delete an existing user if present<br>
    * Otherwise return error saying User not found.
    *
    * @param id
    * @return
    */
  private def deleteUser(id: String): Response =
    users.get(id) match {
      case Some(_) => {
        users -= id
        Response(Message("User deleted."), ResponseCodes.OK)
      }
      case None => Response(Message("User not found."), ResponseCodes.Missing)
    }
}
