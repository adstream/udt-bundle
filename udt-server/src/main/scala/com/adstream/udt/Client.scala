package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.{SocketUDT, TypeUDT, OptionUDT}

object Client extends App {

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

      var count = 0;

      while (true) {

        for (k <- 0 to countBatch) {

          val array:Array[Byte] = (    0 until SIZE).toArray.map(_.toByte)


          val result = sender.send(array);
        }

        // sleep between batches
        Thread.sleep(countSleep);

        count = count + 1;

        if (count % countMonitor == 0) {
          sender.updateMonitor(false);
        }

      }

      // println("result={}", result);

    } catch {
      case e => e.printStackTrace()
    }

  }

  val SIZE = 1460;

}