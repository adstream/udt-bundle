package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.{TypeUDT, SocketUDT}
import net.liftweb.common.Loggable
import UdtPredef._

/**
 * @author Yaroslav Klymko
 */
class UdtClient(val address: InetSocketAddress, config: UdtConfig) extends Loggable {
  val socket = new SocketUDT(TypeUDT.DATAGRAM).configure(config)

  def connect(server: InetSocketAddress) {
    logger.info("UDT Client: %s".format(address))
    socket.bind(address)

    logger.info("Connecting to: %s".format(server))
    socket.connect(server)
  }
}