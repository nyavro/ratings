package com.eny.legalentity

import java.io.OutputStream

trait Document {
  def generate(legalEntity: LegalEntity):OutputStream
}
