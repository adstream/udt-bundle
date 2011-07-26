package com.adstream.udt

import org.specs2.Specification
import akka.actor.Actor._
import java.net.InetSocketAddress

/**
 * @author Yaroslav Klymko
 */
class ServerSpecs extends Specification {
  def is =
    "Server specification" ^
      "server handles multiple connections" ! e1 ^
      end

  def e1 = {
    val serverAddr = new InetSocketAddress(12345)
    val server = actorOf(new FileServer(serverAddr)).start()
    actorOf(new FileClient(new InetSocketAddress(123456), serverAddr)).start()

    success
  }
}