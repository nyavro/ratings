package com.eny.legalentity

import java.util.Date

import com.eny.person.Person

case class LegalEntity(name:String, director:Person, form:EntityForm, inn:String, kpp:String, foundationDate:Date)
