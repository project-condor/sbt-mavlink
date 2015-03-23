package com.github.jodersky.mavlink

object StringUtils {

  private final val Keywords = Set(
  	"class",
  	"object",
  	"trait",
  	"extends",
  	"type",
  	"import",
  	"package",
  	"val",
  	"var",
  	"def",
  	"implicit",
  	"private",
  	"protected",
  	"abstract",
  	"override",
  	"class",
  	"case",
  	"match",
  	"final",
  	"this",
  	"super",
  	"throw",
  	"catch",
  	"finally",
  	"if",
  	"else",
  	"for",
  	"while",
  	"do"
  )

  private def escape(str: String) = if (Keywords.contains(str)) {
  	"`" + str + "`"
  } else {
  	str
  }

  def camelify(str: String) = {
  	val lower = str.toLowerCase
  	escape("_([a-z\\d])".r.replaceAllIn(lower, {m => m.group(1).toUpperCase()}))
  }

  def Camelify(str: String) = {
  	val camel = camelify(str)
  	val (head, tail) = camel.splitAt(1)
  	escape(head.toUpperCase + tail)
  }

}