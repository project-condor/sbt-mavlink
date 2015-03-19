package com.github.jodersky.mavlink

/**
 * X.25 CRC calculation for MAVlink messages. The checksum must be initialized,
 * updated with each field of a message, and then finished with the message
 * id.
 */
case class Crc(val crc: Int = 0xffff) extends AnyVal {

  /**
   * Accumulates data into a new checksum.
   */
  def accumulate(datum: Byte): Crc = {
    val d = datum & 0xff
    var tmp = d ^ (crc & 0xff)
    tmp ^= (tmp << 4) & 0xff;
    Crc(
      ((crc >> 8) & 0xff) ^ (tmp << 8) ^ (tmp << 3) ^ ((tmp >> 4) & 0xff))
  }

  def accumulate(data: Seq[Byte]): Crc = {
    var next = this
    for (d <- data) {
      next = next.accumulate(d)
    }
    next
  }

  /** Least significant byte of checksum. */
  def lsb: Byte = crc.toByte

  /** Most significant byte of checksum. */
  def msb: Byte = (crc >> 8).toByte

}