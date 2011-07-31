package com.adstream.udt

import java.io._
import io.Source

/**
 * @author Yaroslav Klymko
 */
case class TransferInfo(fileName: String, fileSize: Long) {
  def bytes: Array[Byte] = {
    val baos = new ByteArrayOutputStream(8)
    val dos = new DataOutputStream(baos)
    dos.writeLong(fileSize)
    dos.close()
    val bytes = baos.toByteArray
    bytes ++ fileName.getBytes("UTF-8")
  }
}

object TransferInfo {
  def apply(bs: Array[Byte]): TransferInfo = {
    val dis = new DataInputStream(new ByteArrayInputStream(bs))
    try {
      val fileSize = dis.readLong()
      val fileName = Source.fromInputStream(dis, "UTF-8").getLines().next()
      TransferInfo(fileName, fileSize)
    } finally {
      dis.close()
    }
  }

  def apply(file: File): TransferInfo =
    TransferInfo(file.getName, file.length())
}