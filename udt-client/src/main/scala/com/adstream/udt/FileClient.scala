package com.adstream.udt

import net.liftweb.common.Loggable
import scala.App
import scala.Predef._
import java.net.InetSocketAddress
import java.io._
import Predef._
import com.barchart.udt.{OptionUDT, TypeUDT, SocketUDT}
import net.liftweb.util.Props

/**
 * @author Yaroslav Klymko
 */
object FileClient extends App with Loggable {

  de

  def de() {
    // specify server bandwidth limit
    val maxBandwidth = 1
    // specify number of packets sent in a batch
    val countBatch = 30000

    // specify number of millis to sleep between batches of packets
    val countSleep = 1000

    // specify number of packet batches between stats logging
    val countMonitor = 1
    try {

      val sender = new SocketUDT(TypeUDT.DATAGRAM)

      // specify maximum upload speed, bytes/sec
      sender.setOption(OptionUDT.UDT_MAXBW, 30000000L)

      val clientAddress = new InetSocketAddress("localhost", Props.getInt("udt.local.port").openOr(54321))
      logger.info("UDT Client address: " + clientAddress)
      sender.bind(clientAddress)

      val serverAddress = new InetSocketAddress(
        Props.get("udt.server.host").openOr("localhost"),
        Props.getInt("udt.server.port").openOr(12345))
      logger.info("UDT Server address: %s".format(serverAddress))
      sender.connect(serverAddress)

      val path = "C:/Users/Yaroslav Klymko/Downloads/apache-cxf-2.3.3.zip"
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