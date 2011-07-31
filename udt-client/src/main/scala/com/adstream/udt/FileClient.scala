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
import java.text.DecimalFormat

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
    val done = ByteBuffer.wrap(buf).getLong


    val fis = new FileInputStream(file)
    fis.skip(done)


    def x(x: Long) = ((100.0 / file.length()) * x).round
    val mb = (x: Long) => x / 1024 / 1024
    val df = new DecimalFormat("#.##");
    def send(done: Long) {
      val buf = new Array[Byte](udt.socket.getSendBufferSize / 10)
      val read = fis.read(buf)
      if (read == buf.length) {
        val start = System.currentTimeMillis()
        udt.socket.send(buf)
        val time = System.currentTimeMillis() - start
        val before = x(done)
        val progress = x(done + read)
        val speed = read.toDouble / 1024 / 1024 / (time.toDouble / 1000)
        if (progress != before) logger.debug("Progress: " + progress.toString + "% [" + df.format(speed) + "mb/s]")
        send(read + done)
      }
      else {
        udt.socket.send(buf, 0, read)
      }
    }

    val start = System.currentTimeMillis()
    try {
      send(done)
    } finally {
      fis.close()
    }
    val seconds = (System.currentTimeMillis - start) / 1000

    def uploaded = mb(file.length() - done)
    logger.info("Uploaded %smb in: %s seconds. Avarage speed: %smb/s".format(
        uploaded, seconds, uploaded / seconds))

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