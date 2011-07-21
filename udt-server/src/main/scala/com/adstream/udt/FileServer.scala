package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.{TypeUDT, SocketUDT}
import java.io._
import com.adstream.udt.Predef._
import net.liftweb.util.Props
import akka.actor.Actor._
import akka.actor.Actor
import akka.event.EventHandler
import net.liftweb.common.{Box, Loggable}
import util.Properties

/**
 * @author Yaroslav Klymko
 */
object FileServer extends App with Loggable {

  Props.whereToLook = () => {
    val name = "server"
    def stream: Box[InputStream] = Properties.propOrNone("user.dir") match {
      case Some(dir) =>
        val file = new File(dir, name + ".props")
        if (file.exists()) Some(new FileInputStream(file))
        else None
      case _ => None
    }
    (name, () => stream) :: Nil
  }

  val server = actorOf[FileServer].start()
  server ! StartReceiving
}


class FileServer extends Actor with Loggable {

  val socket = new SocketUDT(TypeUDT.DATAGRAM);

  override def preStart() {
    Configuration.configure(socket)

    val serverAddress = new InetSocketAddress("localhost", Props.getInt("udt.server.port", 12345));
    logger.info("UDT Server address: %s".format(serverAddress))
    socket.bind(serverAddress);

    val listenQueueSize = Props.getInt("udt.listen.queue.size", 10)
    logger.info("UDT Listen queue size: %s".format(listenQueueSize))
    socket.listen(listenQueueSize);
  }

  protected def receive = {
    case StartReceiving =>
      EventHandler.debug(this, "StartReceiving")
      actorOf[FileHandler].start ! Receive(socket.accept())
      self ! StartReceiving
    case StopServer => self.stop()
    case unknown => EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }
}

class FileHandler extends Actor with Loggable {
  protected def receive = {
    case Receive(receiver) =>
      EventHandler.debug(this, "Receive")

      val bs = new Array[Byte](1024)
      receiver.receive(bs)

      val tf = TransferInfo(bs)
      val tmp = Props.get("server.out.dir", Properties.propOrEmpty("user.dir"))
      val file = new File(tmp, tf.fileName)

      file.write(bytes => {
        receiver.receive(bytes)
      }, tf.fileSize, tf.packetSize, true)

      val result = new Array[Byte](100)
      val checksum = file.md5Sum
      logger.debug("md5Sum: " + checksum)
      checksum.getBytes("UTF-8").copyToArray(result)
      receiver.send(result)
    case unknown => EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }
}

case class StartReceiving

case class Receive(receiver: SocketUDT)

case class StopServer
