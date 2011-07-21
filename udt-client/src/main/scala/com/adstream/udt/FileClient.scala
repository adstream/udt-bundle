package com.adstream.udt

import scala.App
import scala.Predef._
import java.net.InetSocketAddress
import java.io._
import Predef._
import net.liftweb.util.Props
import com.barchart.udt.{TypeUDT, SocketUDT}
import akka.actor.Actor
import akka.event.EventHandler
import net.liftweb.common.{Box, Loggable}
import util.Properties

/**
 * @author Yaroslav Klymko
 */

object FileClient extends App with Loggable {
  if (args.isEmpty) logger.error("File path is not provided")
  else {

    Props.whereToLook = () => {
      val name = "client"
      def stream: Box[InputStream] = Properties.propOrNone("user.dir") match {
        case Some(dir) =>
          val file = new File(dir, name + ".props")
          if (file.exists()) Some(new FileInputStream(file))
          else None
        case _ => None
      }
      (name, () => stream) :: Nil
    }

    val path = args(0)
    Actor.actorOf[FileClient].start() !! SendFile(new File(path))
    Actor.registry.shutdownAll()
  }
}

class FileClient extends Actor with Loggable {

  protected def receive = {
    case SendFile(file) => send(file)
    case unknown => EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }

  def send(file: File) {
    val sender = Configuration.configure(new SocketUDT(TypeUDT.DATAGRAM))

    val clientAddress = new InetSocketAddress("localhost", Props.getInt("udt.local.port", 54321))
    logger.info("UDT Client address: " + clientAddress)
    sender.bind(clientAddress)

    val serverAddress = new InetSocketAddress(
      Props.get("udt.server.host", "localhost"),
      Props.getInt("udt.server.port", 12345))
    logger.info("UDT Server address: %s".format(serverAddress))
    sender.connect(serverAddress)

    val ti = TransferInfo(file, 1024 * 100)

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