package com.adstream.udt

import java.net.InetAddress
import akka.event.EventHandler
import akka.actor.Actor._
import akka.actor.Actor
import akka.routing._
import udt.{UDTOutputStream, UDTInputStream, UDTSocket, UDTServerSocket}
import io.Source
import java.io._
import scala.tools.nsc.io.Streamable
import sun.misc.Resource
import collection.mutable.ListBuffer

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

  val server = actorOf[Server]
  server.start()
  server ! "connect"
}

class Server extends Actor {
  val handler = actorOf[Handler]
  lazy val serverSocket = new UDTServerSocket(InetAddress.getByName("localhost"), 65321)

  override def preStart() {
    handler.start()
  }

  override def postStop() {
    handler.stop()
  }

  def receive = {
    case "connect" =>
      EventHandler.info(this, "Connected")
            handler ! serverSocket.accept
      self ! "connect"
    case msg => EventHandler.info(this, "Received unknown message: " + msg)
  }
}

class Handler extends Actor with RoundRobinSelector with FixedCapacityStrategy {

  def selectionCount = 1

  def partialFill = true

  def limit = 2

  protected def receive = {
    case socket: UDTSocket =>
      EventHandler.info(this, "UDTSocket: " + socket)
      val in = socket.getInputStream
      val out = socket.getOutputStream
      val buf = ListBuffer[Byte]()
      val b = in.read()
      while (b >= 0 && b != 10) {
        println(b)
        buf += b.toByte
      }

      val bytes = buf.toArray


      println(bytes.mkString("\n"))
      out.write(bytes)
      out.flush()
//      Source.fromInputStream(in).getLines().map{
//        line =>
//        println("ECHO: " + line)
//        writer.w(line)
//        writer.flush
//      }
      println("Request from <" + socket.getSession.getDestination + "> finished.")

    case msg => EventHandler.info(this, "Received unknown message: " + msg)
  }

}