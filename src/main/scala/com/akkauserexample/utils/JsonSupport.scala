package com.akkauserexample.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.akkauserexample.actors.UserHandler.Message
import com.akkauserexample.model.{Address, Organisation, User}

trait JsonSupport extends SprayJsonSupport {
  import spray.json.DefaultJsonProtocol._

  implicit val addressJsonFormat = jsonFormat5(Address)
  implicit val organisationJsonFormat = jsonFormat5(Organisation)
  implicit val userJsonFormat = jsonFormat9(User)
  implicit val messageJsonFormat = jsonFormat1(Message)
}
