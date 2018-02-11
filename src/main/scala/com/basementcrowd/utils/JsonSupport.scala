package com.basementcrowd.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.basementcrowd.actors.UserActor.Message
import com.basementcrowd.model.{Address, Organisation, User}

trait JsonSupport extends SprayJsonSupport {
  import spray.json.DefaultJsonProtocol._

  implicit val addressJsonFormat = jsonFormat5(Address)
  implicit val organisationJsonFormat = jsonFormat5(Organisation)
  implicit val userJsonFormat = jsonFormat9(User)
  implicit val actionPerformedJsonFormat = jsonFormat1(Message)
}
