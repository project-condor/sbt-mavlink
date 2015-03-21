package com.github.jodersky.mavlink

import scala.xml.Node

trait Reporter {

  protected def printWarning(msg: String): Unit

  def fatal(error: String, node: Node) = throw new ParseError("Parse error: " + error + " at " + node)
  def warn(warning: String, node: Node) =  printWarning("Warning: " + warning +" at " + node)

}

object StandardReporter extends Reporter {
  protected def printWarning(msg: String): Unit = Console.err.println(msg)
}