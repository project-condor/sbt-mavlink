package com.github.jodersky.mavlink

object StringUtils {

  def camelify(str: String) = {
  	val lower = str.toLowerCase
  	"_([a-z\\d])".r.replaceAllIn(lower, {m => m.group(1).toUpperCase()})
  }

  def Camelify(str: String) = {
  	val camel = camelify(str)
  	val (head, tail) = camel.splitAt(1)
  	head.toUpperCase + tail
  }

}