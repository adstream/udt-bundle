package com.adstream.udt

import net.liftweb.common.Loggable
import scala.App
import scala.Predef._
import java.net.InetSocketAddress
import java.io._
import Predef._
import net.liftweb.util.Props
import com.barchart.udt.{TypeUDT, SocketUDT}

/**
 * @author Yaroslav Klymko
 */
object FileClient extends App with Loggable  {

  start()

  def start() {
    val sender = Configuration.configure(new SocketUDT(TypeUDT.DATAGRAM))

    val clientAddress = new InetSocketAddress("localhost", Props.getInt("udt.local.port", 54321))
    logger.info("UDT Client address: " + clientAddress)
    sender.bind(clientAddress)

    val serverAddress = new InetSocketAddress(
      Props.get("udt.server.host", "localhost"),
      Props.getInt("udt.server.port", 12345))
    logger.info("UDT Server address: %s".format(serverAddress))
    sender.connect(serverAddress)

    val path = "C:\\Users\\t3hnar\\Downloads\\scala-2.9.0.1-installer.jar"
    val file = new File(path)
    val tf = TransferInfo(file, 1024*10)

    sender.send(tf.bytes)

    file.read(bytes => {
      logger.debug("bytes.length: " + bytes.length)
      val result = sender.send(bytes)
      logger.debug("result: "+ result)
//      assert(result == bytes.length)
    }, tf.packetSize)

    sender.receive(new Array[Byte](2))
  }
}