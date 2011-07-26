package com.adstream.udt

import net.liftweb.util.Props
import net.liftweb.common.Loggable
import com.barchart.udt.{LingerUDT, OptionUDT, SocketUDT}
import OptionUDT._

/**
 * @author Yaroslav Klymko
 */
object UdtProps extends Loggable {

  def apply(): UdtProps = new UdtProps(
    Props.getInt("udt.max.packet.size", Default.udtMaxPacketSize),
    Props.getInt("udt.window.size", Default.udtWindowSize),
    Props.getBool("udt.send.sync", Default.udtSendSync),
    Props.getBool("udt.receive.sync", Default.udtReceiveSync),
    Props.getInt("udt.send.buffer", Default.udtSendBuffer),
    Props.getInt("udt.receive.buffer", Default.udtReceiveBuffer),
    Props.getInt("udp.send.buffer", Default.udpSendBuffer),
    Props.getInt("udp.receive.buffer", Default.udpReceiveBuffer),
    Props.getInt("udt.linger").map(new LingerUDT(_)).openOr(Default.udtLinger),
    Props.getBool("udt.rendezvous", Default.udtRendezvous),
    Props.getInt("udt.send.timeout", Default.udtSendTimeout),
    Props.getInt("udt.receive.timeout", Default.udtReceiveTimeout),
    Props.getBool("udt.reuse.address", Default.udtReuseAddress),
    Props.getLong("udt.max.bandwidth", Default.udtMaxBandwidth))

  val Default = new UdtProps(1500, 25600, true, true,
    10240000, 10240000, 10240000, 10240000,
    new LingerUDT(180), false, -1, -1, true, -1)


  def get[T](key: String, valueOf: String => T): T = {
    val value = valueOf(key)
    logger.info("%s: %s".format(key, value))
    value
  }

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

case class UdtProps(udtMaxPacketSize: Int,
                    udtWindowSize: Int,
                    udtSendSync: Boolean,
                    udtReceiveSync: Boolean,
                    udtSendBuffer: Int,
                    udtReceiveBuffer: Int,
                    udpSendBuffer: Int,
                    udpReceiveBuffer: Int,
                    udtLinger: LingerUDT,
                    udtRendezvous: Boolean,
                    udtSendTimeout: Int,
                    udtReceiveTimeout: Int,
                    udtReuseAddress: Boolean,
                    udtMaxBandwidth: Long)

