package com.adstream.udt

import org.specs2.SpecificationWithJUnit
import com.adstream.udt.Predef._
import java.io.File

/**
 * @author Yaroslav Klymko
 */
class RichFileSpec extends SpecificationWithJUnit {
  def is =
    "RichFile specification" ^
      "calculates md5 checksum" ! e1 ^
      end

  def e1 = new File(getClass.getResource("rich-file.txt").getFile).md5Sum mustEqual "df28fdf0fe17b47b1215373d1d95f886"
}