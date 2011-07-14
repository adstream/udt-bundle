package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.{TypeUDT, SocketUDT}
import net.liftweb.common.Loggable
import java.io._
import com.adstream.udt.Predef._
import net.liftweb.util.Props

/**
 * @author Yaroslav Klymko
 */
object FileServer extends App with Loggable {
  main

  def main {
    // specify how many packets must come before stats logging
    val countMonitor = 30000

    try {
      val acceptor = new SocketUDT(TypeUDT.DATAGRAM);

      val serverAddress = new InetSocketAddress("localhost", Props.getInt("udt.server.port").openOr(12345));
      logger.info("UDT Server address: %s".format(serverAddress))
      acceptor.bind(serverAddress);

      val listenQueueSize = Props.getInt("udt.listen.queue.size").openOr(1)
      logger.info("UDT Liten queue size: %s".format(listenQueueSize))
      acceptor.listen(listenQueueSize);

      val receiver = acceptor.accept()
      val remoteSocketAddress = receiver.getRemoteSocketAddress;

      val bs = new Array[Byte](1024)
      receiver.receive(bs)

      val tf = TransferInfo(bs)
      val file = new File("D:\\Projects\\adstream\\udt-bundle", tf.fileName)

      file.write(bytes => {
        receiver.receive(bytes)
      }, tf.fileSize, tf.packetSize)

    } catch {
      case e => logger.error(e.getMessage, e);
    }
  }
}