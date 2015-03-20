import org.mavlink._
import org.mavlink.messages._

object Main {

  val SenderSystemId = 0: Byte
  val SenderComponentId = 0: Byte
  val ReceiverSystemId = 1: Byte
  val ReceiverComponentId = 0: Byte

  def main(args: Array[String]): Unit = {
	echoTest()
  }

  def echoTest() = {
  	//represents the line buffer, i.e. all data going in and out
  	val line = new Array[Byte](Packet.MaxPacketLength)

  	//payload of incoming messages
  	val in = MavlinkBuffer.allocate()

  	//parser to transform an incoming byte stream into packets
  	val parser = new Parser(in)(
  	  pckt => {
  	    val msg = Message.unpack(pckt.messageId, pckt.payload)
  	    println("received message: " + msg)
  	  },
  	  err => {
  	  	sys.error("parse error: " + err)
  	  }
  	)

  	//payload buffer of outgoing messages
  	val out = MavlinkBuffer.allocate()

  	//assembles messages into pakets from a specific sender
  	val assembler = new Assembler(SenderSystemId, SenderComponentId)

  	//create an explicit message
  	val message = Heartbeat(0)

  	//pack the message into a payload
  	val id = Message.pack(message, out)

  	//assemble into packet
  	val packet = assembler.assemble(id, out)

  	//simulate wire transfer
  	packet.writeTo(line)
  	parser.push(line)
  }

}