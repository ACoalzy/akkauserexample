package com.akkauserexample.utils

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.FunSuite

class ResponseCodesTest extends FunSuite {

  test("OK maps to http OK") {
    assert(ResponseCodes.responseToHTTP(ResponseCodes.OK) == StatusCodes.OK)
  }

  test("Created maps to http Created") {
    assert(ResponseCodes.responseToHTTP(ResponseCodes.Created) == StatusCodes.Created)
  }

  test("Conflict maps to http Conflict") {
    assert(ResponseCodes.responseToHTTP(ResponseCodes.Conflict) == StatusCodes.Conflict)
  }

  test("Missing maps to http NotFound") {
    assert(ResponseCodes.responseToHTTP(ResponseCodes.Missing) == StatusCodes.NotFound)
  }

}
