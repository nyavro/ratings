package com.eny.utils.formatting

abstract class NameFormat(last:String, name:String, middle:String) {
  def format():String
}

class LastFMFormat(last:String, name:String, middle:String) extends NameFormat(last, name, middle) {
  lazy val format = s"$last ${name.charAt(0).toUpper}. ${middle.charAt(0).toUpper}."
}
