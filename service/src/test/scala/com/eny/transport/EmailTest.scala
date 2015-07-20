package com.eny.transport

import org.scalatest.{FlatSpec, Matchers}

class EmailTest extends FlatSpec with Matchers {

  ignore should "Send mail" in {
    val cfg = SMTPConfig("e.nyavro", "secret", "smtp.googlemail.com", false, false, 465)
    val mail = Email(cfg, "Hello", "evg1174@mail.ru", "e.nyavro@gmail.com", "hello from eny!")
    mail.send()
  }
}
