package com.adstream.udt

import net.liftweb.common.Loggable
import java.io.{BufferedInputStream, FileInputStream, File}

/**
 * @author Yaroslav Klymko
 */
object ReadFile extends App with Loggable {


  val path = "C:/Users/t3hnar/Downloads/Amerikanskaya.Istoriya.Iks.1998.DUAL.BDRip.XviD.AC3.-HQCLUB/Amerikanskaya.Istoriya.Iks.1998.DUAL.BDRip.XviD.AC3.-HQCLUB.avi"
  val file = new File(path)
  val fileSize = file.length()

  val fis = new FileInputStream(file)
  //  val bis = new BufferedInputStream(fis, Const.packageSize)


  def sendPackets(read: Long, left: Long): Unit = if (left > 0) {
    val size = math.min(Const.packageSize, left)
    val array = new Array[Byte](size.toInt)
    fis.read(array)
    //    logger.debug("array.last: " + array.last)
    logger.debug("read: " + read / 1000000)
    logger.debug("left: " + left / 1000000)
    sendPackets(read + size, left - size)
    //        bis.read()

  }

  val start = System.currentTimeMillis()
  sendPackets(0, fileSize)

  logger.debug("Done in: " + (System.currentTimeMillis - start) / 1000)
}