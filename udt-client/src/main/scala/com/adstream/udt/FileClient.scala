package com.adstream.udt

import net.liftweb.common.Loggable
import scala.App
import scala.Predef._
import java.net.InetSocketAddress
import java.io._
import Predef._
import com.barchart.udt.{OptionUDT, TypeUDT, SocketUDT}

/**
 * @author Yaroslav Klymko
 */
object FileClient extends App with Loggable {

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

      // specify maximum upload speed, bytes/sec
      sender.setOption(OptionUDT.UDT_MAXBW, 30000000L);

      val localSocketAddress = new InetSocketAddress(bindAddress, 54321);

      println("localSocketAddress : {}", localSocketAddress);

      sender.bind(localSocketAddress);
      println("bind; localSocketAddress={}", localSocketAddress);

      val remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);

      sender.connect(remoteSocketAddress);
      println("connect; remoteSocketAddress={}", remoteSocketAddress);

      val path = "C:/Users/t3hnar/Downloads/SKIDROW.rar"
      val file = new File(path)
      val tf = TransferInfo(file, 1024 * 10)

      sender.send(tf.bytes)

      file.read(bytes => {
        assert(sender.send(bytes) == bytes.length)
      }, tf.packetSize)
    } catch {
      case e => e.printStackTrace()
    }

  }


}