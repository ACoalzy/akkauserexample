package com.akkauserexample.model

final case class Address(
                          id: String,
                          line1: String,
                          line2: String,
                          city: String,
                          postCode: String
                        )