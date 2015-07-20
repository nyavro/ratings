package com.eny.repository

import java.util.UUID

import scala.concurrent.Future

trait Repository[T] {
  def insertRecord(record: T): Future[Boolean]

  def getById(id: UUID): Future[Option[T]]
}
