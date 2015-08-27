package com.eny.utils.formatting

import java.util.Date

import org.joda.time.DateTime

abstract class DateFormat(date:Date) {
  def format():String
}

class GenitiveCaseDateFormat(date:Date) extends DateFormat(date) {
  lazy val dt = new DateTime(date.getTime)
  lazy val months = List("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
  lazy val format = s"${dt.getDayOfMonth} ${months(dt.monthOfYear.get - 1)} ${dt.year.get}"
}
