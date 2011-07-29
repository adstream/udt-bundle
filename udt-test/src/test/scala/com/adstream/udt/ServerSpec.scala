package com.adstream.udt

import org.specs2.Specification
import akka.actor.Actor._
import java.net.InetSocketAddress
import util.Properties
import java.io.File

/**
 * @author Yaroslav Klymko
 */
class ServerSpec extends Specification {
  def is =
    "Server specification" ^
      "server handles multiple connections" ! e1 ^
      end

  def e1 = {
    val serverAddr = new InetSocketAddress("localhost",12345)
    val server = actorOf(new FileServer(serverAddr, Properties.tmpDir)).start()
    server ! StartReceiving
    def client = actorOf(new FileClient(new InetSocketAddress(0), serverAddr)).start()

    client ! SendFile(new File("C:\\Users\\t3hnar\\Downloads\\scala-2.9.0.1-installer.jar"))
    client ! SendFile(new File("C:\\Users\\t3hnar\\Downloads\\nb-driver-T1125N-T1125M-WLAN-8.0.0.372.zip"))
    client ! SendFile(new File("C:\\Users\\t3hnar\\Downloads\\mysql-5.5.14-winx64.msi"))

    success
  }
}