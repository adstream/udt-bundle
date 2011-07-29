package com.adstream.udt

import scala.App
import scala.Predef._
import java.io._
import Predef._
import net.liftweb.util.Props
import com.barchart.udt.{TypeUDT, SocketUDT}
import akka.actor.Actor
import akka.event.EventHandler
import net.liftweb.common.Loggable
import java.net.InetSocketAddress

/**
 * @author Yaroslav Klymko
 */
object FileClient extends App with Loggable with PropsOutside {

  if (args.isEmpty) logger.error("File path is not provided")
  else {
    val path = args(0)
    val clientAddr = new InetSocketAddress(Props.getInt("udt.local.port", 0))
    val serverAddr = new InetSocketAddress(
      Props.get("udt.server.host", "localhost"),
      Props.getInt("udt.server.port", 12345))

    Actor.actorOf(new FileClient(clientAddr, serverAddr)).start() !! SendFile(new File(path))
    Actor.registry.shutdownAll()
  }

  def propsName = "client.props"
}

class FileClient(val address: InetSocketAddress, val serverAddress: InetSocketAddress) extends Actor with Loggable {

  protected def receive = {
    case SendFile(file) => send(file)
    case unknown => EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }

  def send(file: File) {
    val sender = UdtProps.configure(new SocketUDT(TypeUDT.DATAGRAM))

    logger.info("UDT Client address: " + address)
    sender.bind(address)

    logger.info("UDT Server address: %s".format(serverAddress))
    sender.connect(serverAddress)

    val ti = TransferInfo(file, sender.getSendBufferSize/10)

    sender.send(ti.bytes)

    file.read(bytes => {
      val result = sender.send(bytes)
      assert(result == bytes.length)
    }, ti.packetSize, true)

    val checksum = new Array[Byte](100)
    sender.receive(checksum)

    val current = file.md5Sum
    logger.info("current checksum: " + current)

    val result = new String(checksum).trim()
    logger.info("result checksum: " + result)

    if (result == current) logger.info("SUCCESS: checksums equal")
    else logger.info("FAIL: checksums not equal")
  }
}

case class SendFile(file: File)