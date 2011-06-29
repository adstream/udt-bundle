package com.adstream.udt

import java.net.InetAddress
import udt.{UDTOutputStream, UDTInputStream, UDTServerSocket}
import udt.util.Util
import java.io.{FileOutputStream, OutputStreamWriter, PrintWriter}

/**
 * @author Yaroslav Klymko
 */
object Server extends App {
  val server = new UDTServerSocket(InetAddress.getByName("localhost"), 65321)
  val socket = server.accept()
  System.out.println("Processing request from <" + socket.getSession.getDestination + ">")
  var in: UDTInputStream = socket.getInputStream
  var out: UDTOutputStream = socket.getOutputStream
  var writer = new FileOutputStream("C:\\Users\\t3hnar\\Projects\\adstream\\udt-bundle\\test.avi")
  val buffer = new Array[Byte](0x1000);
  while (true) {
    val read = in.read(buffer, 0, buffer.size)
    if (read > 0)
      writer.write(buffer, 0, read)
  }
}