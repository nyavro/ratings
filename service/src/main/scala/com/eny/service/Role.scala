package com.eny.service

object Role extends Enumeration {
  type Role = Value
  val User = Value("user")
  val Admin = Value("admin")
  val Super = Value("super")
}


