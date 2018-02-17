package com.akkauserexample.model

final case class Organisation(
                               id: String,
                               name: String,
                               email: String,
                               `type`: String,
                               address: Address
                             )