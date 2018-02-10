package com.basementcrowd.actors

import akka.actor.{Actor, Props}

final case class Address(id: String, line1: String, line2: String, city: String, postCode: String)
final case class Organisation(id: String, name: String, email: String, `type`: String, address: Address)
case class User(id: String, organisation: Organisation, address: Address, firstName: String, lastName: String, email: String, salutation: String, telephone: String, `type`: String)

object UserActor {
  final case class GetUser(id: String)
  final case class CreateUser(user: User)
  final case class UpdateUser(user: User)
  final case class DeleteUser(id: String)

  def props(users: Map[String, User]): Props = Props(new UserActor(users))
}

class UserActor(initialUsers: Map[String, User]) extends Actor {
  import com.basementcrowd.actors.UserActor._

  var users: Map[String, User] = initialUsers

  override def receive: Receive = {
    case GetUser(id) => sender() ! users.get(id)
    case CreateUser(user) => {
      if (users.keySet.exists(_ == user.id)) sender() ! false
      else {
        users += user.id -> user
        sender() ! true
      }
    }
    case UpdateUser(user) => {
      if (users.keySet.exists(_ == user.id)) {
        users += user.id -> user
        sender() ! true
      }
      else sender() ! false
    }
    case DeleteUser(id) =>
      if (users.keySet.exists(_ == id)) {
        users -= id
        sender() ! true
      }
      else sender() ! false
  }
}
