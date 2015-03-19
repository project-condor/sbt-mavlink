package com.github.jodersky.mavlink

import scala.io.Source
import scala.xml.XML
import trees._

object MainTest {
  
  def main(args: Array[String]): Unit = {
    val definition = XML.load(getClass.getResource("/concise.xml"))
    val dialect = Parser.parseDialect(definition)
    val generator = new Generator(dialect)
    println(generator.generate())
  }

}