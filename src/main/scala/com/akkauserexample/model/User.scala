package com.akkauserexample.model

case class User(
                 id: String,
                 organisation: Organisation,
                 address: Address,
                 firstName: String,
                 lastName: String,
                 email: String,
                 salutation: String,
                 telephone: String,
                 `type`: String
               )