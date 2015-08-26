package com.eny.legalentity

import java.io._
import java.util
import java.util.{Locale, Date}
import java.util.zip.{ZipFile, ZipEntry, ZipOutputStream}

import com.eny.person.Person
import org.apache.commons.io.IOUtils
import org.apache.commons.io.filefilter.HiddenFileFilter
import org.apache.commons.lang3.time.{DateFormatUtils, FastDateFormat}
import org.joda.time.DateTime
import scala.collection.JavaConversions._
import scala.io.Source

class PositionCommand extends Document {

  val TemplateName = "position/template.docx"
  val DocumentName = "document.xml"

  override def generate(legalEntity: LegalEntity): OutputStream = {
    val out = new FileOutputStream("/home/eny/out.docx")
    val zip = new ZipOutputStream(out)

    def formatDateGen(date:Date) = {
      val jdate = new DateTime(date.getTime)
      val months = List("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
      s"${jdate.getDayOfMonth} ${months(jdate.monthOfYear.get)} ${jdate.year.get}"
    }

    val map = Map(
      "entity_name" -> legalEntity.name,
      "entity_inn" -> legalEntity.inn,
      "entity_kpp" -> legalEntity.kpp,
      "order_number" -> 1.toString,
      "entity_form_short" -> legalEntity.form.short,
      "entity_form_full" -> legalEntity.form.full,
      "directors_last_fm" -> s"${legalEntity.director.last} ${legalEntity.director.name.charAt(0).toUpper}. ${legalEntity.director.middle.charAt(0).toUpper}.",
      "entity_found_gen" -> formatDateGen(legalEntity.foundationDate),
      "order_date_gen" -> formatDateGen(legalEntity.foundationDate)
    )

    def replace(src:String) = {
      val res = map.foldLeft(src) {
        case (acc, (key, value)) => acc.replaceAll(key, value)
      }
      res
    }

    def substitute(stream: InputStream) = {
      Source.fromInputStream(stream, "UTF-8").getLines().foreach {
        item => IOUtils.copy(new StringReader(replace(item)), zip)
      }
    }
    val zipInput = new ZipFile(getClass.getClassLoader.getResource(TemplateName).getFile)
    zipInput.entries().toList.map {
      entry => {
        zip.putNextEntry(new ZipEntry(entry.getName))
        if(entry.getName.endsWith(DocumentName))
          substitute(zipInput.getInputStream(entry))
        else
          IOUtils.copy(zipInput.getInputStream(entry), zip)
        zip.closeEntry()
      }
    }
    zip.close()
    zip
  }
}

object PositionCommand {
  def main (args: Array[String]) {
    val command: PositionCommand = new PositionCommand()
    command.generate(LegalEntity("RACK-MASTER", Person("Ivanov", "Иван", "Иванович"), OOO, "123", "456", new Date))
  }
}