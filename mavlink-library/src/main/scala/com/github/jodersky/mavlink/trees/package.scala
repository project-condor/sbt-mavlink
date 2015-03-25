package com.github.jodersky.mavlink

package trees {
  
  sealed trait Tree
  
  case class Dialect(version: Option[String], enums: Set[Enum], messages: Set[Message]) extends Tree
  case class Enum(name: String, entries: Seq[EnumEntry], description: String) extends Tree
  case class EnumEntry(value: Int, name: String, description: String) extends Tree
  case class Field(tpe: Type, nativeType: String, name: String, enum: Option[String], description: String) extends Tree
  case class Message(id: Byte, name: String, description: String, fields: Seq[Field]) extends Tree {
    def orderedFields = fields.toSeq.sortBy(_.tpe.width)(Ordering[Int].reverse)
    def length = fields.map(_.tpe.sizeof).sum

    lazy val checksum = {
      var c = new Crc()
      c = c.accumulate((name + " ").getBytes)
      for (field <- orderedFields) {
        c = c.accumulate((field.nativeType + " ").getBytes)
        c = c.accumulate((field.name + " ").getBytes)
      }
      (c.lsb ^ c.msb).toByte
    }
  }
  
  trait Type extends Tree {
    def width: Int // width in bytes of the type
    def sizeof: Int = width // size of bytes of the type
  }

  case class IntType(width: Int, signed: Boolean) extends Type
  case class FloatType(width: Int) extends Type
  case class ArrayType(underlying: Type, length: Int) extends Type {
    def width = underlying.width
    override def sizeof = width * length
  }
  case object CharType extends Type {
    def width = 1
  }
  case class StringType(maxLength: Int) extends Type {
    val width = 1
    override def sizeof = width * maxLength
  }

}