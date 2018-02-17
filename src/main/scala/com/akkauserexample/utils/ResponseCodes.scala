package com.akkauserexample.utils

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

object ResponseCodes {
  sealed trait ResponseCode
  case object OK extends ResponseCode
  case object Created extends ResponseCode
  case object Conflict extends ResponseCode
  case object Missing extends ResponseCode

  def responseToHTTP(code: ResponseCode): StatusCode = code match {
    case OK => StatusCodes.OK
    case Created => StatusCodes.Created
    case Conflict => StatusCodes.Conflict
    case Missing => StatusCodes.NotFound
  }
}
