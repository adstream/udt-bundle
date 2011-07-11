package com.adstream.udt

import java.io._
import io.Source

/**
 * @author Yaroslav Klymko
 */
case class TransferInfo(fileName: String, fileSize: Long, packetSize: Int) {
  lazy val bytes: Array[Byte] = {
    val baos = new ByteArrayOutputStream(8 + 4)
    val dos = new DataOutputStream(baos)
    dos.writeLong(fileSize)
    dos.writeInt(packetSize)
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
      val packetSize = dis.readInt()
      val fileName = Source.fromInputStream(dis, "UTF-8").getLines().next()
      TransferInfo(fileName, fileSize, packetSize)
    } finally {
      dis.close()
    }
  }

  def apply(file: File, packetSize: Int): TransferInfo =
    TransferInfo(file.getName, file.length(), packetSize)
}