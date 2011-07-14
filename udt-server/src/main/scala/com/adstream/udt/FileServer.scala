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
  start()

  def start() {
    //TODO TypeUDT.DATAGRAM vs TypeUDT.STREAM
    val acceptor = new SocketUDT(TypeUDT.DATAGRAM);

    Configuration.configure(acceptor)

    val serverAddress = new InetSocketAddress("localhost", Props.getInt("udt.server.port", 12345));
    logger.info("UDT Server address: %s".format(serverAddress))
    acceptor.bind(serverAddress);

    val listenQueueSize = Props.getInt("udt.listen.queue.size", 10)
    logger.info("UDT Listen queue size: %s".format(listenQueueSize))
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
  }
}