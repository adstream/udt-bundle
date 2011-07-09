package com.adstream.udt

import akka.event.EventHandler
import akka.actor.Actor._
import akka.actor.Actor
import akka.routing._
import io.Source
import scala.tools.nsc.io.Streamable
import sun.misc.Resource
import collection.mutable.ListBuffer
import java.net.{InetSocketAddress, InetAddress}
import com.barchart.udt.{MonitorUDT, OptionUDT, TypeUDT, SocketUDT}
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import util.Properties
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation
import net.liftweb.common.Loggable
import java.io._
import com.adstream.udt.Predef._

/**
 * @author Yaroslav Klymko
 */
object Server extends App with Loggable {
  main

  def main {

    println("started SERVER");

    // specify server listening interface
    val bindAddress = "localhost"

    // specify server listening port
    val localPort = 12345

    // specify how many packets must come before stats logging
    val countMonitor = 30000

    try {

      val SIZE = 1460;

      val acceptor = new SocketUDT(TypeUDT.DATAGRAM);
      acceptor.configureBlocking(true)

      val localSocketAddress = new InetSocketAddress(
        bindAddress, localPort);

      acceptor.bind(localSocketAddress);
      println("bind; localSocketAddress={} " + localSocketAddress);

      acceptor.listen(1);
      println("listen")

      val receiver = acceptor.accept()

      val timeStart = System.currentTimeMillis();

      //

      val remoteSocketAddress = receiver.getRemoteSocketAddress;


      //      receiver

      //      println("receiver; remoteSocketAddress={} " + remoteSocketAddress);


      val bs = new Array[Byte](Const.firstMessageSize)
      receiver.receive(bs)

      val dis = new DataInputStream(new ByteArrayInputStream(bs))
      val fileSize = dis.readLong()
      logger.debug("fileSize: " + fileSize)
      val packetSize = dis.readInt()
      logger.debug("packetSize: " + packetSize)

      //TODO
      val isr = new InputStreamReader(dis, "UTF-8")
      val chars = new Array[Char](Const.stringSize)
      isr.read(chars)
      val fileName = new String(chars)
      logger.debug("fileName: " + fileName)
      //      isr.close()

      val file = new File("C:\\Users\\t3hnar\\Projects\\adstream\\udt-bundle\\test.avi")


      logger.debug("receiver.getReceiveBufferSize: " + receiver.getReceiveBufferSize)
      //      val bytes = new Array[Byte](packetSize)
      //      acceptor.receive(bytes)
      //      receiver.receive(bytes)
      //      receiver.getReceiveBufferSize

      //      logger.debug("bytes.head: " + bytes.head + "; bytes.last: " + bytes.last)



      file.write(bytes => {
//        Thread.sleep(1000)
        logger.debug("receive bytes.length: " + bytes.length)
        receiver.receive(bytes)
        logger.debug("bytes.head: " + bytes.head + "; bytes.last: " + bytes.last)
      }, fileSize, packetSize)
      //


      //      receiver.send()


      //      while (true) {
      //
      //        val array = new Array[Byte](SIZE)
      //        val result = receiver.receive(array);
      //        println("result: " + result)
      //        println(array.mkString)
      //        println("last: " + array.last)
      //        //        println("receive rate, bytes/second: {}", rate);
      //
      //        Thread.sleep(1000);
      //      }

      // log.info("result={}", result);

    } catch {
      case e => println("unexpected", e);
      e.printStackTrace()
    }

  }


}