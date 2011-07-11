package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.{TypeUDT, SocketUDT}
import net.liftweb.common.Loggable
import java.io._
import com.adstream.udt.Predef._

/**
 * @author Yaroslav Klymko
 */
object FileServer extends App with Loggable {
  main

  def main {

    println("started SERVER");

    // specify server listening interface
    val bindAddress = "localhost"

    // specify server listening port
    val localPort = 12345

    // specify how many packets must come before stats logging
    val countMonitor = 30000

    try {
      val acceptor = new SocketUDT(TypeUDT.DATAGRAM);

      val localSocketAddress = new InetSocketAddress(
        bindAddress, localPort);

      acceptor.bind(localSocketAddress);
      println("bind; localSocketAddress={} " + localSocketAddress);

      acceptor.listen(1);
      println("listen")

      val receiver = acceptor.accept()

      val timeStart = System.currentTimeMillis();

      //

      val remoteSocketAddress = receiver.getRemoteSocketAddress;

      val bs = new Array[Byte](1024)
      receiver.receive(bs)

      val tf = TransferInfo(bs)
      val file = new File("C:\\Users\\t3hnar\\Projects\\adstream\\udt-bundle", tf.fileName)

      file.write(bytes => {
        receiver.receive(bytes)
      }, tf.fileSize, tf.packetSize)

    } catch {
      case e => println("unexpected", e);
      e.printStackTrace()
    }
  }
}