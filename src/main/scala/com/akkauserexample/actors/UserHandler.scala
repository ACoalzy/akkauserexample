package com.akkauserexample.actors

import com.akkauserexample.model.User
import com.akkauserexample.utils.ResponseCodes.ResponseCode

object UserHandler {
  sealed trait CRUD
  final case class GetUser(id: String) extends CRUD
  final case class CreateUser(user: User) extends CRUD
  final case class UpdateUser(id: String, user: User) extends CRUD
  final case class DeleteUser(id: String) extends CRUD

  final case class Response(message: Message, responseCode: ResponseCode)
  final case class Message(msg: String)
}
