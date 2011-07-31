package com.adstream.udt

import org.specs2.SpecificationWithJUnit
import java.io.File

/**
 * @author Yaroslav Klymko
 */
class TransferInfoSpec extends SpecificationWithJUnit {
  def is =
    "TransferInfo contains file name, size, size of packets, etc" ^
      "retrieves info from file" ! e1 ^
      "converts to bytes and retrieves from bytes" ! e2 ^
      end

  val file = new File(getClass.getResource("transfer-info.txt").getFile)

  def e1 = TransferInfo(file) mustEqual TransferInfo("transfer-info.txt", 36)

  def e2 = {
    val tf = TransferInfo(file)
    TransferInfo(tf.bytes) mustEqual tf
  }
}