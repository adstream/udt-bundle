package com.adstream.udt

import java.net.InetSocketAddress
import com.barchart.udt.SocketUDT
import com.adstream.udt.UdtPredef._
import net.liftweb.util.Props
import akka.actor.Actor._
import akka.actor.Actor
import akka.event.EventHandler
import net.liftweb.common.Loggable
import util.Properties
import java.nio.ByteBuffer
import java.io._
import collection.mutable.Buffer
import org.specs2.internal.scalaz.Validation
import java.text.DecimalFormat

/**
 * @author Yaroslav Klymko
 */
object FileServer extends App with Loggable with PropsOutside {

  def propsName = "server.props"

  val address: InetSocketAddress = new InetSocketAddress(Props.getInt("udt.server.port", 12345))
  val udt = new UdtServer(address, UdtConfig.FromProps)
  val out = Props.get("server.out.dir", Properties.propOrEmpty("user.dir"))
  val server = actorOf(new FileServer(udt, out, Props.getInt("udt.listen.queue.size", 10))).start()
}

class FileServer(val server: UdtServer, val outDir: String, queueSize: Int = 10) extends Actor with Loggable {
  val Accept = new AnyRef

  override def preStart() {
    server.listen(queueSize)
    self ! Accept
  }

  protected def receive = {
    case Accept =>
      EventHandler.debug(this, "Accepting...")
      accept()
      self ! Accept
    case unknown =>
      EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }

  private def accept() {
    val accepted = server.socket.accept()
    EventHandler.debug(this, "Accepted...")
    actorOf(new FileReceiver(outDir)).start ! Receive(accepted)
  }
}

class FileReceiver(val outDir: String) extends Actor with Loggable {
  protected def receive = {
    case Receive(receiver) =>
      EventHandler.debug(this, "Uploading")

      val bs = new Array[Byte](1024)
      receiver.receive(bs)
      val tf = TransferInfo(bs)

      val name = "tmp_" + tf.fileName
      val file = new File(outDir, name)

      val done =
        if (file.exists() && file.length() != tf.fileSize) file.length()
        else 0
      receiver.send(ByteBuffer.wrap(new Array[Byte](8)).putLong(done).array())

      def resume = done != 0

      val mb = (x: Long) => x / 1024 / 1024
      if (resume) logger.info( "Resuming upload. Done: %smb".format(mb(done)))
      else logger.info("Uploading %smb".format(mb(tf.fileSize)))

      val fos = new FileOutputStream(file, resume)

      def x(x: Long) = ((100.0 / tf.fileSize) * x).round

      val df = new DecimalFormat("#.##");
      def write() {
        val buf = new Array[Byte](receiver.getReceiveBufferSize / 10)

        val start = System.currentTimeMillis()
        val received = receiver.receive(buf)
        val time = System.currentTimeMillis() - start

        if (received == buf.length) {
          val before = x(file.length())
          val progress = x(file.length() + received)
          val speed = received.toDouble / 1024 / 1024 / (time.toDouble / 1000)
          if (progress != before) logger.debug("Progress: " + progress.toString + "% [" + df.format(speed) + "mb/s]")
          fos.write(buf)
          write()
        } else {
          fos.write(buf, 0, received)
        }
      }

      val start = System.currentTimeMillis()
      try {
        write()
      } finally {
        fos.close()
      }
      val seconds = (System.currentTimeMillis - start) / 1000

      val uploaded = mb(file.length - done)
      logger.info("Uploaded %smb in: %s seconds. Avarage speed: %smb/s".format(
        uploaded, seconds, uploaded / seconds))

      val checksum = file.md5Sum
      logger.debug("md5Sum: " + checksum)
      receiver.send(checksum.getBytes("UTF-8"))
      file.renameTo(new File(outDir, tf.fileName))
    case unknown => EventHandler.warning(this, "Unknown message: %s".format(unknown))
  }
}

case class Receive(receiver: SocketUDT)