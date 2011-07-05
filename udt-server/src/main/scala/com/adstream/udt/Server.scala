package com.adstream.udt

import akka.event.EventHandler
import akka.actor.Actor._
import akka.actor.Actor
import akka.routing._
import io.Source
import java.io._
import scala.tools.nsc.io.Streamable
import sun.misc.Resource
import collection.mutable.ListBuffer
import java.net.{InetSocketAddress, InetAddress}
import com.barchart.udt.{MonitorUDT, OptionUDT, TypeUDT, SocketUDT}
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import util.Properties

/**
 * @author Yaroslav Klymko
 */
object Server extends App {

  println(System.getProperty("os.name"))
  println(System.getProperty("os.arch"))

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

      val acceptor = new SocketUDT(TypeUDT.DATAGRAM);

      val localSocketAddress = new InetSocketAddress(
        bindAddress, localPort);

      acceptor.bind(localSocketAddress);
      println("bind; localSocketAddress={} " + localSocketAddress);

      acceptor.listen(1000);
      println("listen;")

      val receiver = acceptor.accept()

      val timeStart = System.currentTimeMillis();

      //

      val remoteSocketAddress = receiver.getRemoteSocketAddress();

      println("receiver; remoteSocketAddress={} " + remoteSocketAddress);

      while (true) {

        val array = new Array[Byte](SIZE)

        val result = receiver.receive(array);

        assert(result == SIZE, "wrong size")


        getSequenceNumber(array)

        if (sequenceNumber % countMonitor == 0) {

          receiver.updateMonitor(false);
          val timeFinish = System.currentTimeMillis();
          val timeDiff = 1 + (timeFinish - timeStart) / 1000;

          val byteCount = sequenceNumber * SIZE;
          val rate = byteCount / timeDiff;

          println("receive rate, bytes/second: {}", rate);

        }

      }

      // log.info("result={}", result);

    } catch {
      case e => println("unexpected", e);
      e.printStackTrace()
    }

  }

  var sequenceNumber = 0;

  def getSequenceNumber(array: Array[Byte]) {

    val buffer = ByteBuffer.wrap(array);

    val currentNumber = buffer.getLong();

    if (currentNumber == sequenceNumber) {
      sequenceNumber = sequenceNumber +1;
    } else {
      println("sequence error; currentNumber={} sequenceNumber={} " + sequenceNumber);
      System.exit(1);
    }

  }

  val sequencNumber = new AtomicLong(0);

  val SIZE = 1460;


}

class Server extends Actor {
  val socket = new SocketUDT(TypeUDT.DATAGRAM)
  val handler = actorOf[Handler]

  override def preStart() {
    handler.start()
  }

  override def postStop() {
    handler.stop()
  }

  def receive = {
    case "connect" =>
      EventHandler.info(this, "Connected")
      handler ! socket.accept()
      self ! "connect"
    case msg => EventHandler.info(this, "Received unknown message: " + msg)
  }
}

class Handler extends Actor with RoundRobinSelector with FixedCapacityStrategy {

  def selectionCount = 1

  def partialFill = true

  def limit = 2

  protected def receive = {
    case socket: SocketUDT =>
      EventHandler.info(this, "Received unknown message: " + socket)

    case msg => EventHandler.info(this, "Received unknown message: " + msg)
  }
}