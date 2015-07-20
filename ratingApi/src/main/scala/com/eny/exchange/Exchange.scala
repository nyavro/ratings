package com.eny.exchange

import java.io.{OutputStream, InputStream}

trait ResourceType

trait Exchange {

  def `import`(source:InputStream)(proc: (String, InputStream) => Unit):Unit

  def export[A](chunks:Map[String, A], target:OutputStream)(proc: (String, A, OutputStream) => Unit): Unit
}