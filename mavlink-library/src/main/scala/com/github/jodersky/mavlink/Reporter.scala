package com.github.jodersky.mavlink

import java.io.File

import scala.xml.Node

trait Reporter {

  protected def printWarning(msg: String): Unit

  def fatal(message: String, node: Node, file: File) = throw new ParseError(s"Parse error: $message at $node in file ${file.getAbsolutePath}")
  def warn(message: String, node: Node, file: File) =  printWarning(s"Parse warning: $message at ${node} in file ${file.getAbsolutePath}")

}

object StandardReporter extends Reporter {
  protected def printWarning(msg: String): Unit = Console.err.println(msg)
}