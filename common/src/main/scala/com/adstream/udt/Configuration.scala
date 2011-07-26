package com.adstream.udt

import net.liftweb.util.Props
import net.liftweb.common.Loggable
import com.barchart.udt.{LingerUDT, OptionUDT, SocketUDT}
import OptionUDT._

/**
 * @author Yaroslav Klymko
 */
object Configuration extends Loggable {

  def configure(socket: SocketUDT): SocketUDT = {

    def set[T](option: OptionUDT, key: String, valueOf: String => T) {
      val value = valueOf(key)
      logger.info("%s: %s".format(key, value))
      socket.setOption(option, value)
    }

    set(UDT_MSS, "udt.max.packet.size", Props.getInt(_, 1500))
    set(UDT_FC, "udt.window.size", Props.getInt(_, 25600))

    set(UDT_SNDSYN, "udt.send.sync", Props.getBool(_, true))
    set(UDT_RCVSYN, "udt.receive.sync", Props.getBool(_, true))

    set(UDT_SNDBUF, "udt.send.buffer", Props.getInt(_, 10240000))
    set(UDT_RCVBUF, "udt.receive.buffer", Props.getInt(_, 10240000))

    set(UDP_SNDBUF, "udp.send.buffer", Props.getInt(_, 10240000))
    set(UDP_RCVBUF, "udp.receive.buffer", Props.getInt(_, 10240000))

    set(UDT_LINGER, "udt.linger", key => new LingerUDT(Props.getInt(key, 180)))
    set(UDT_RENDEZVOUS, "udt.rendezvous", Props.getBool(_, false))

    set(UDT_SNDTIMEO, "udt.send.timeout", Props.getInt(_, -1))
    set(UDT_RCVTIMEO, "udt.receive.timeout", Props.getInt(_, -1))

    set(UDT_REUSEADDR, "udt.reuse.address", Props.getBool(_, true))

    set(UDT_MAXBW, "udt.max.bandwidth", Props.getLong(_, -1))

    socket
  }
}