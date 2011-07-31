package com.adstream.udt

import scala.App
import scala.Predef._
import UdtPredef._
import net.liftweb.util.Props
import akka.actor.Actor
import akka.event.EventHandler
import net.liftweb.common.Loggable
import java.net.InetSocketAddress
import java.io._
import java.nio.ByteBuffer

/**
 * @author Yaroslav Klymko
 */
object FileClient extends App with Loggable with PropsOutside {

  if (args.isEmpty) logger.error("File path is not provided")
  else {
    val path = args(0)
    val address = new InetSocketAddress(Props.getInt("udt.local.port", 0))
    val serverAddr = new InetSocketAddress(
      Props.get("udt.server.host", "localhost"),
      Props.getInt("udt.server.port", 12345))

    val udt = new UdtClient(address, UdtConfig.FromProps)
    Actor.actorOf(new FileClient(udt, serverAddr)).start() !! SendFile(new File(path))
    Actor.registry.shutdownAll()
  }

  def propsName = "client.props"
}

class FileClient(val udt: UdtClient, val server: InetSocketAddress) extends Actor with Loggable {

  override def preStart() {
    udt.connect(server)
  }

  protected def receive = {
    case SendFile(file) => send(file)
    case unknown => EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }

  def send(file: File) {
    val ti = TransferInfo(file)

    udt.socket.send(ti.bytes)

    val buf = new Array[Byte](8)
    udt.socket.receive(buf)
    val index = ByteBuffer.wrap(buf).getLong


    val fis = new FileInputStream(file)
    fis.skip(index)


    def send() {
      val buf = new Array[Byte](udt.socket.getSendBufferSize / 10)
      val read = fis.read(buf)
      logger.debug(read.toString)
      if (read == buf.length) {
        udt.socket.send(buf)
        send()
      }
      else {
        udt.socket.send(buf, 0, read)
      }
    }

    try {
      send()
    } finally {
      fis.close()
    }

//    file.read(bytes => {
//      val result = udt.socket.send(bytes)
//      assert(result == bytes.length)
//    },  true)

    val checksum = new Array[Byte](100)
    udt.socket.receive(checksum)

    val current = file.md5Sum
    logger.info("current checksum: " + current)

    val result = new String(checksum).trim()
    logger.info("result checksum: " + result)

    if (result == current) logger.info("SUCCESS: checksums equal")
    else logger.info("FAIL: checksums not equal")
  }
}

case class SendFile(file: File)