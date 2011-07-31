package com.adstream.udt

import com.barchart.udt.{TypeUDT, SocketUDT}
import net.liftweb.common.Loggable
import java.net.InetSocketAddress
import net.liftweb.util.Props
import UdtPredef._

/**
 * @author Yaroslav Klymko
 */
class UdtServer(val address: InetSocketAddress, val config: UdtConfig = UdtConfig.Default) extends Loggable {

  val socket = new SocketUDT(TypeUDT.DATAGRAM).configure(config)

  def listen(listenQueueSize: Int = 10) {
    logger.info("Binding UDT Server")
    logger.info("UDT Server: %s".format(address))
    socket.bind(address);

    val size = Props.getInt("udt.listen.queue.size", 10)
    logger.info("Listen queue size: %s".format(size))
    socket.listen(size)
  }
}