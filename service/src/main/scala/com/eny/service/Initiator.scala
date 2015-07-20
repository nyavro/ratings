package com.eny.service

import com.eny.model.UserID
import com.eny.service.Role.Role

case class Initiator(
  userId:UserID,
  roles:Set[Role]
)
