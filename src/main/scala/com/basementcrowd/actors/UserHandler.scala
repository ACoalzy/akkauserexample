package com.basementcrowd.actors

import com.basementcrowd.model.User

object UserHandler {
  type MsgResult = Either[Message, Message]

  sealed trait CRUD
  final case class GetUser(id: String) extends CRUD
  final case class CreateUser(user: User) extends CRUD
  final case class UpdateUser(id: String, user: User) extends CRUD
  final case class DeleteUser(id: String) extends CRUD

  case class Message(msg: String)
}
