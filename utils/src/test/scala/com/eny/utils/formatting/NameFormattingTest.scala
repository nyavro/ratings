package com.eny.utils.formatting

import org.scalatest.{Matchers, WordSpecLike}

class NameFormattingTest extends WordSpecLike with Matchers {
  "LastFMFormat" must {
    "Format name" in {
      new LastFMFormat("Petrov", "Ivan", "Vasilievich").format should === ("Petrov I. V.")
      new LastFMFormat("Петров", "Иван", "Васильевич").format should === ("Петров И. В.")
    }
  }
}
