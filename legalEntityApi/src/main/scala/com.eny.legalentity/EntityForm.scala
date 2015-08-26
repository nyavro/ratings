package com.eny.legalentity

sealed trait EntityForm {
  def short:String
  def full:String
}

case object OOO extends EntityForm {
  val short = "ООО"
  val full = "Общество с ограниченной ответственностью"
}

