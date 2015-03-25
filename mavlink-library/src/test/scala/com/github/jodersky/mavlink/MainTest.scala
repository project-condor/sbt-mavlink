package com.github.jodersky.mavlink

import java.io.File

object MainTest {
  
  def main(args: Array[String]): Unit = {
    val parser: Parser = new Parser(StandardReporter)
    val dialectDefinitionFile: File = new File("mavlink-library/src/test/resources/including.xml")
    val dialect = parser.parseDialect(dialectDefinitionFile)
    val generator = new Generator(dialect)
    println(generator.targets.map(_.generate()).mkString("\n\n"))
  }

}