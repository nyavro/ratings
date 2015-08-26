package com.eny.legalentity

import java.io.{ByteArrayOutputStream, OutputStream}
import java.util.zip.ZipOutputStream

class PositionCommandTest extends Document {

   val TemplateName = "position"

   override def generate(legalEntity: LegalEntity): OutputStream = {
     val out = ByteArrayOutputStream
     val zip = new ZipOutputStream(out)

     zip
   }
 }
