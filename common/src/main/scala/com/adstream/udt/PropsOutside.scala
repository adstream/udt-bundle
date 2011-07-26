package com.adstream.udt

import net.liftweb.util.Props
import net.liftweb.common.Box
import util.Properties
import java.io.{FileInputStream, File, InputStream}

/**
 * @author Yaroslav Klymko
 */
trait PropsOutside {
  Props.whereToLook = () => {
    def stream: Box[InputStream] = Properties.propOrNone("user.dir") match {
      case Some(dir) =>
        val file = new File(dir, propsName)
        if (file.exists()) Some(new FileInputStream(file))
        else None
      case _ => None
    }
    (propsName, () => stream) :: Nil
  }

  def propsName: String
}