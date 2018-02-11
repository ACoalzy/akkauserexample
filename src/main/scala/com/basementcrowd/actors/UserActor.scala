package com.basementcrowd.actors

import akka.actor.{Actor, Props}
import com.basementcrowd.model.User

object UserActor {
  type MsgResult = Either[Message, Message]

  final case class GetUser(id: String)
  final case class CreateUser(user: User)
  final case class UpdateUser(id: String, user: User)
  final case class DeleteUser(id: String)

  case class Message(msg: String)

  def props(users: Map[String, User]): Props = Props(new UserActor(users))
}

class UserActor(initialUsers: Map[String, User]) extends Actor {
  import com.basementcrowd.actors.UserActor._

  var users: Map[String, User] = initialUsers

  override def receive: Receive = {
    case GetUser(id) => sender() ! users.get(id)
    case CreateUser(user) => {
      if (users.keySet.exists(_ == user.id)) sender() ! Left(Message("Cannot create User as User with that ID already exists."))
      else {
        users += user.id -> user
        sender() ! Right(Message("User created."))
      }
    }
    case UpdateUser(id, user) => {
      if (id != user.id && users.keySet.exists(_ == user.id)) sender() ! Left(Message("New User ID clashes with another user."))
      else if (users.keySet.exists(_ == id)) {
        users -= id
        users += user.id -> user
        sender() ! Right(Message("User updated."))
      }
      else sender() ! Left(Message("User not found."))
    }
    case DeleteUser(id) =>
      if (users.keySet.exists(_ == id)) {
        users -= id
        sender() ! Right(Message("User deleted."))
      }
      else sender() ! Left(Message("User not found."))
  }
}
