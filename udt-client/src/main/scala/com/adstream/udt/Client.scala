package com.adstream.udt

import com.barchart.udt.{SocketUDT, TypeUDT}
import java.net.{InetSocketAddress, InetAddress}
import java.io.{OutputStreamWriter, PrintWriter}
import udt.UDTClient
import udt.util.Util
import io.Source

/**
 * @author Yaroslav Klymko
 */
object Client extends App {
//  var client = new UDTClient(InetAddress.getLocalHost, 12345)
//  client.connect("localhost", 65321)
//
//  client.getOutputStream
  val path = "C:\\Users\\t3hnar\\Downloads\\Видео\\keepern.til.liverpool.2010.hdrip.avi"
//  client.getInputStream.setBlocking(false)

  val stream = Source.fromFile(path).toStream


//    while (true) {
//    val read = in.read(buffer, 0, buffer.size)
//    if (read > 0)
//      writer.write(buffer, 0, read)
//  }



  //  val localhost = new InetSocketAddress(InetAddress.getLocalHost, 12345)
  //  val server = new SocketUDT(TypeUDT.DATAGRAM)
  //  server.connect(localhost)
  //
  //  println("connected: " + server.isConnected)


  //  val socket = new SocketUDT(TypeUDT.DATAGRAM)

  //  socket.connect(localhost)


}