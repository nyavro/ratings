package com.eny.utils.formatting

import java.text.SimpleDateFormat
import java.util.Locale

import org.scalatest.{Matchers, WordSpecLike}

class DateFormattingTest extends WordSpecLike with Matchers {
  "DateFormatter" must {
    "Format date in russian genitive case" in {
      val dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US)
      new GenitiveCaseDateFormat(dateFormat.parse("01.02.2000")).format should === ("1 февраля 2000")
      new GenitiveCaseDateFormat(dateFormat.parse("31.12.2010")).format should === ("31 декабря 2010")
      new GenitiveCaseDateFormat(dateFormat.parse("30.01.2010")).format should === ("30 января 2010")
      new GenitiveCaseDateFormat(dateFormat.parse("01.01.2010")).format should === ("1 января 2010")
      new GenitiveCaseDateFormat(dateFormat.parse("31.08.2010")).format should === ("31 августа 2010")
    }
  }
}
