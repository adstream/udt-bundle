package com.adstream.udt

import java.security.MessageDigest
import java.io.{FileOutputStream, FileInputStream, File}
import net.liftweb.common.Loggable

/**
 * @author Yaroslav Klymko
 */
object Predef {
  implicit def fileToRichFile(f: File): RichFile = new RichFile(f)

  implicit def richFileToFile(rf: RichFile): File = rf.file
}

case class RichFile(file: File) extends Loggable {
  lazy val md5Sum: String = {
    val md5 = MessageDigest.getInstance("MD5");
    read(md5.update(_))
    md5.digest().map(0xFF & _).map("%02x".format(_)).foldLeft("")(_ + _)
  }

  def read(f: Array[Byte] => Unit, bufferSize: Int = RichFile.BufferSize, log: Boolean = false) {
    val fis = new FileInputStream(file);

    def read(buffer: Array[Byte]) {
      assert(fis.read(buffer) == buffer.length)
      f(buffer)
    }

    try {
      process(read, file.length(), bufferSize, log)
    } finally {
      fis.close()
    }
  }

  def write(f: Array[Byte] => Unit, fileSize: Long, bufferSize: Int = RichFile.BufferSize, log: Boolean = false) {
    val fos = new FileOutputStream(file)

    def write(buffer: Array[Byte]) {
      f(buffer)
      fos.write(buffer)
    }

    try {
      process(write, fileSize, bufferSize, log)
    } finally {
      fos.close()
    }
  }

  private def process(f: Array[Byte] => Unit, fileSize: Long, bufferSize: Int = RichFile.BufferSize, log: Boolean = false) {
    val x = 100.0 / fileSize

    def process(done: Long) {
      if (done < fileSize) {
        val buffer = new Array[Byte](math.min(bufferSize, fileSize - done).toInt)
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
  val BufferSize = 1024 * 1024 * 10
}