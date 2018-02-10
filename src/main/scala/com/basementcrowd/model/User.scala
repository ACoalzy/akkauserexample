package com.basementcrowd.model

final case class Address(id: String, line1: String, line2: String, city: String, postCode: String)
final case class Organisation(id: String, name: String, email: String, `type`: String, address: Address)
case class User(id: String, organisation: Organisation, address: Address, firstName: String, lastName: String, email: String, salutation: String, telephone: String, `type`: String)