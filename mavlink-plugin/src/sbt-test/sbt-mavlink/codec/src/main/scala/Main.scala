import org.mavlink._
import org.mavlink.messages._

object Main {

  val SenderSystemId = 0: Byte
  val SenderComponentId = 0: Byte
  val ReceiverSystemId = 1: Byte
  val ReceiverComponentId = 0: Byte

  def main(args: Array[String]): Unit = {
  	println("Echo test")
	echoTest()
  }

  def echoTest() = {

  	//parser to transform an incoming byte stream into packets
  	val parser = new Parser(
  	  (pckt: Packet) => {
  	    val msg: Message = Message.unpack(pckt.messageId, pckt.payload)
  	    println("received message: " + msg)
  	  },
  	  (err: Parser.Errors.Error) => {
  	  	sys.error("parse error: " + err)
  	  }
  	)

  	//assembles messages into packets from a specific sender
  	val assembler = new Assembler(SenderSystemId, SenderComponentId)

  	//create an explicit message
  	val message = TestMessage(
  	  Array[Short](1,2),
  	  Array.fill[Float](20)(0.2f),
  	  3: Byte,
  	  42.0,
  	  "hello world"
  	)

  	//pack the message into a payload
  	val (id: Byte, payload: Array[Byte]) = Message.pack(message)

  	//assemble into packet
  	val packet = assembler.assemble(id, payload)

  	//simulate wire transfer
  	val data = packet.toArray
  	parser.push(data)
  }
  
}