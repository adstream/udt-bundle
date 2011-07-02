package com.adstream.udt

import java.net.InetAddress
import akka.event.EventHandler
import akka.actor.Actor._
import akka.actor.Actor
import akka.routing._
import io.Source
import java.io._
import scala.tools.nsc.io.Streamable
import sun.misc.Resource
import collection.mutable.ListBuffer
import com.barchart.udt.{TypeUDT, SocketUDT}

/**
 * @author Yaroslav Klymko
 */
object Server extends App {



  //  val server = new UDTServerSocket(InetAddress.getByName("localhost"), 65321)
  //  val socket = server.accept()
  //  System.out.println("Processing request from <" + socket.getSession.getDestination + ">")
  //  var in: UDTInputStream = socket.getInputStream
  //  var out: UDTOutputStream = socket.getOutputStream
  //  var writer = new FileOutputStream("C:\\Users\\t3hnar\\Projects\\adstream\\udt-bundle\\test.avi")
  //  val buffer = new Array[Byte](0x1000);
  //  while (true) {
  //    val read = in.read(buffer, 0, buffer.size)
  //    if (read > 0)
  //      writer.write(buffer, 0, read)
  //  }

//  val server = actorOf[Server]
//  server.start()
//  server ! "connect"

  println(System.getProperty("os.name"))

	/** The Constant OS_ARCH. */
  val arch = "os.arch"
	println(System.getProperty(arch))

  System.setProperty(arch,"x86_64")
  println(System.getProperty(arch))

  //TODO from config
  val socket = new SocketUDT(TypeUDT.DATAGRAM)
  val id = socket.getSocketId





}

class Server extends Actor {
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
//      self ! "connect"
    case msg => EventHandler.info(this, "Received unknown message: " + msg)
  }
}

class Handler extends Actor with RoundRobinSelector with FixedCapacityStrategy {

  def selectionCount = 1

  def partialFill = true

  def limit = 2

  protected def receive = {
     case msg => EventHandler.info(this, "Received unknown message: " + msg)
  }

}