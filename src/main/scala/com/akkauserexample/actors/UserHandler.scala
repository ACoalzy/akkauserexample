package com.akkauserexample.actors

import akka.http.scaladsl.model.StatusCode
import com.akkauserexample.model.User

object UserHandler {
  sealed trait CRUD
  final case class GetUser(id: String) extends CRUD
  final case class CreateUser(user: User) extends CRUD
  final case class UpdateUser(id: String, user: User) extends CRUD
  final case class DeleteUser(id: String) extends CRUD

  final case class Message(msg: String)
  final case class Response(message: Message, statusCode: StatusCode)
}
