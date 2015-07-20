package com.eny.transport

import org.apache.commons.mail.{DefaultAuthenticator, HtmlEmail}

case class SMTPConfig(
  user : String,
  password: String,
  host : String,
  tls : Boolean = false,
  ssl : Boolean = false,
  port : Int = 25
)

case class Email(
  cfg : SMTPConfig,
  subject: String,
  recipient: String,
  from: String,
  text: String
) {
  
  def send() = {
    val email = new HtmlEmail()
    email.setStartTLSEnabled(cfg.tls)
    email.setSSLOnConnect(cfg.ssl)
    email.setSmtpPort(cfg.port)
    email.setHostName(cfg.host)
    email.setAuthenticator(
      new DefaultAuthenticator(
        cfg.user,
        cfg.password
      )
    )
    email
      .setHtmlMsg(text)
      .addTo(recipient)
      .setFrom(from)
      .setSubject(subject)
      .send()
  }
}