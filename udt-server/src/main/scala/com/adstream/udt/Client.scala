package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.{SocketUDT, TypeUDT, OptionUDT}
import util.Random
import net.liftweb.common.Loggable
import java.io._
import org.specs2.internal.scalaz.Validation
import sun.reflect.generics.tree.BooleanSignature
import com.adstream.udt.Predef._

object Client extends App with Loggable {

  de

  def de() {

    println("started CLIENT");

    // specify client sender interface
    val bindAddress = "localhost"

    // specify server listening address
    val remoteAddress = "localhost"

    // specify server listening port
    val remotePort = 12345

    // specify server bandwidth limit
    val maxBandwidth = 1
    // specify number of packets sent in a batch
    val countBatch = 30000

    // specify number of millis to sleep between batches of packets
    val countSleep = 1000

    // specify number of packet batches between stats logging
    val countMonitor = 1
    try {

      val sender = new SocketUDT(TypeUDT.DATAGRAM);
      sender.configureBlocking(true)

      // specify maximum upload speed, bytes/sec
      sender.setOption(OptionUDT.UDT_MAXBW, 30000000L);

      val localSocketAddress = new InetSocketAddress(bindAddress, 54321);

      println("localSocketAddress : {}", localSocketAddress);

      sender.bind(localSocketAddress);
      println("bind; localSocketAddress={}", localSocketAddress);

      val remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);

      sender.connect(remoteSocketAddress);
      println("connect; remoteSocketAddress={}", remoteSocketAddress);

      var count = 0;

      //      val bs = new Array[Byte](Const.firstMessageSize)


      val path = "C:/Users/t3hnar/Downloads/gpsies.apk"
      val file = new File(path)
      val fileSize = file.length()
      logger.debug("fileSize: " + fileSize)

      val baos = new ByteArrayOutputStream(Const.firstMessageSize)
      val dos = new DataOutputStream(baos)
      dos.writeLong(fileSize)
      val packetSize = Const.packageSize
      logger.debug("packetSize: " + packetSize)
      dos.writeInt(packetSize)
      val osw = new OutputStreamWriter(dos)

      val fileName = file.getName
      logger.debug("fileName: " + fileName)
      val chars = new Array[Char](Const.stringSize)
      fileName.toCharArray.copyToArray(chars)
      osw.write(chars)
      osw.flush()
//      osw.close()

      sender.send(baos.toByteArray)


      file.read(bytes => {
//        Thread.sleep(1000)
        logger.debug("send bytes.length: " + bytes.length)
        logger.debug(bytes.mkString)
        assert(sender.send(bytes) == bytes.length)
      }, packetSize)
    } catch {
      case e => e.printStackTrace()
    }

  }


}