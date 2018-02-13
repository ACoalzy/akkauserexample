package com.basementcrowd.actors

import akka.actor.{Actor, Props}
import com.basementcrowd.model.{Address, Organisation, User}

object UserMapActor {
  def props(users: Map[String, User], orgs: Map[String, Organisation], addrs: Map[String, Address]): Props =
    Props(new UserMapActor(users, orgs, addrs))
}

class UserMapActor(initUsers: Map[String, User], initOrgs: Map[String, Organisation], initAddr: Map[String, Address]) extends Actor {
  import com.basementcrowd.actors.UserHandler._

  var users: Map[String, User] = initUsers
  var organisations: Map[String, Organisation] = initOrgs
  var addresses: Map[String, Address] = initAddr

  override def receive: Receive = {
    case GetUser(id) => sender() ! users.get(id)
    case CreateUser(user) => sender() ! createUser(user)
    case UpdateUser(id, user) => sender() ! updateUser(id, user)
    case DeleteUser(id) => sender() ! deleteUser(id)
  }

  /**
    * Returns user with updated organisation and address if both present<br>
    * Otherwise returns error message detailing which is missing
    *
    * @param user
    * @return
    */
  private def getOrgAndAddress(user: User): Either[Message, User] = {
    val org = organisations.get(user.organisation.id)
    val address = addresses.get(user.address.id)
    (org, address) match {
      case (Some(o), Some(a)) => {
        Right(user.copy(organisation = o, address = a))
      }
      case (None, _) => Left(Message("Organisation ID doesn't match existing Organisation."))
      case (_, None) => Left(Message("Address ID doesn't match existing Address."))
    }
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
  private def createUser(user: User): MsgResult =
    users.get(user.id) match {
      case Some(_) => Left(Message("Cannot create User as User with that ID already exists."))
      case None => getOrgAndAddress(user) match {
        case Right(user) => {
          users += user.id -> user
          Right(Message("User created."))
        }
        case Left(m) => Left(m)
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
  private def updateUser(id: String, user: User): MsgResult = {
    val toUpdate = users.get(id)
    val updateTo = if (id != user.id) users.get(user.id) else None
    (toUpdate, updateTo) match {
      case (None, _) => Left(Message("User not found."))
      case (_, Some(_)) => Left(Message("New User ID clashes with another user."))
      case (Some(_), _) => getOrgAndAddress(user) match {
        case Right(user) => {
          users -= id
          users += user.id -> user
          Right(Message("User updated."))
        }
        case Left(m) => Left(m)
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
  private def deleteUser(id: String): MsgResult =
    users.get(id) match {
      case Some(_) => {
        users -= id
        Right(Message("User deleted."))
      }
      case None => Left(Message("User not found."))
    }
}
