package com.eny.utils.io
import scala.util.Try

trait Resources {

  def managed[A,B](resource: => A)(cleanup: A => Unit)(code: A => B): Try[B] =
    Try {
      val r = resource
      try {
        code(r)
      }
      finally {
        cleanup(r)
      }
    }
}
