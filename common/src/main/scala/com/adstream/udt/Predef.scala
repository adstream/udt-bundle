package com.adstream.udt

import java.security.MessageDigest
import java.io.{FileOutputStream, FileInputStream, File}

/**
 * @author Yaroslav Klymko
 */
object Predef {
  implicit def fileToRichFile(f: File): RichFile = new RichFile(f)

  implicit def richFileToFile(rf: RichFile): File = rf.file
}

case class RichFile(file: File) {
  lazy val md5Sum: String = {
    val md5 = MessageDigest.getInstance("MD5");
    read(md5.update(_))
    md5.digest().map(0xFF & _).map("%02x".format(_)).foldLeft("")(_ + _)
  }

  def read(f: Array[Byte] => Unit, bufferSize: Int = RichFile.BufferSize) {
    val fis = new FileInputStream(file);

    def read(buffer: Array[Byte]) {
      assert(fis.read(buffer) == buffer.length)
      f(buffer)
    }

    try {
      process(read, file.length(), bufferSize)
    } finally {
      fis.close()
    }
  }

  def write(f: Array[Byte] => Unit, fileSize: Long, bufferSize: Int = RichFile.BufferSize) {
    val fos = new FileOutputStream(file)

    def write(buffer: Array[Byte]) {
      f(buffer)
      fos.write(buffer)
    }

    try {
      process(write, fileSize, bufferSize)
    } finally {
      fos.close()
    }
  }

  private def process(f: Array[Byte] => Unit, fileSize: Long, bufferSize: Int = RichFile.BufferSize) {
    def process(done: Long) {
      if (done < fileSize) {
        val buffer = new Array[Byte](math.min(bufferSize, fileSize - done).toInt)
        f(buffer)
        process(done + buffer.length)
      }
    }
    process(0)
  }
}

object RichFile {
  val BufferSize = 1024 * 1024 * 10
}