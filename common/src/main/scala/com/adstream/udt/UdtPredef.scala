package com.adstream.udt

import java.security.MessageDigest
import java.io.{FileOutputStream, FileInputStream, File}
import net.liftweb.common.Loggable
import com.barchart.udt.OptionUDT._
import com.barchart.udt.SocketUDT

/**
 * @author Yaroslav Klymko
 */
object UdtPredef {
  implicit def fileToRichFile(f: File): RichFile = new RichFile(f)

  implicit def richFileToFile(rf: RichFile): File = rf.file

  implicit def socketUdt2RichSocketUdt(socket: SocketUDT) = new RichSocketUdt(socket)

  implicit def richSocketUdt2SocketUdt(richSocket: RichSocketUdt) = richSocket.socket
}

class RichSocketUdt(val socket: SocketUDT) {
  def configure(config: UdtConfig): RichSocketUdt = {
    socket.setOption(UDT_MSS, config.udtMaxPacketSize)
    socket.setOption(UDT_FC, config.udtWindowSize)

    socket.setOption(UDT_SNDSYN, config.udtSendSync)
    socket.setOption(UDT_RCVSYN, config.udtReceiveSync)

    socket.setOption(UDT_SNDBUF, config.udtSendBuffer)
    socket.setOption(UDT_RCVBUF, config.udtReceiveBuffer)

    socket.setOption(UDP_SNDBUF, config.udpSendBuffer)
    socket.setOption(UDP_RCVBUF, config.udpReceiveBuffer)

    socket.setOption(UDT_LINGER, config.udtLinger)
    socket.setOption(UDT_RENDEZVOUS, config.udtRendezvous)

    socket.setOption(UDT_SNDTIMEO, config.udtSendTimeout)
    socket.setOption(UDT_RCVTIMEO, config.udtReceiveTimeout)

    socket.setOption(UDT_REUSEADDR, config.udtReuseAddress)

    socket.setOption(UDT_MAXBW, config.udtMaxBandwidth)

    this
  }
}

case class RichFile(file: File) extends Loggable {
  lazy val md5Sum: String = {
    val md5 = MessageDigest.getInstance("MD5");
    read(md5.update(_))
    md5.digest().map(0xFF & _).map("%02x".format(_)).foldLeft("")(_ + _)
  }

  def read(f: Array[Byte] => Unit,  log: Boolean = false) {
    val fis = new FileInputStream(file);

    def read(buffer: Array[Byte]) {
      assert(fis.read(buffer) == buffer.length)
      f(buffer)
    }

    try {
      process(read, file.length(),  log)
    } finally {
      fis.close()
    }
  }

  def write(f: Array[Byte] => Unit, fileSize: Long, log: Boolean = false) {
    val fos = new FileOutputStream(file, true)

    def write(buffer: Array[Byte]) {
      f(buffer)
      fos.write(buffer)
    }

    try {
      process(write, fileSize, log)
    } finally {
      fos.close()
    }
  }

  private def process(f: Array[Byte] => Unit, fileSize: Long,  log: Boolean = false) {
    val x = 100.0 / fileSize

    def process(done: Long) {
      if (done < fileSize) {
        val buffer = new Array[Byte](math.min(RichFile.BufferSize, fileSize - done).toInt)
        f(buffer)

        if (log) {
          val progress = (done * x).toInt
          val before = ((done - buffer.length) * x).toInt
          if (progress != before) logger.debug("Progress: " + progress.toString + "%")
        }

        process(done + buffer.length)
      } else if (log) logger.debug("Progress: 100%")
    }
    process(0)
  }
}

object RichFile {
  val BufferSize = 1024 * 1024
}