package com.adstream.udt

import net.liftweb.util.Props
import net.liftweb.common.Loggable
import com.barchart.udt.{LingerUDT, OptionUDT, SocketUDT}
import OptionUDT._

/**
 * @author Yaroslav Klymko
 */
object UdtConfig extends Loggable {

  val Default = new UdtConfig(1500, 25600, true, true,
    10240000, 10240000, 10240000, 10240000,
    new LingerUDT(180), false, -1, -1, true, -1)

  lazy val FromProps = new UdtConfig(
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
}

case class UdtConfig(udtMaxPacketSize: Int,
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

