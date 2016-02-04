# SBT-MAVLink Plugin

This plugin provides generation of Scala sources from MAVLink message definition files.
It translates a MAVLink dialect defined in XML to useable scala objects and provides utilities for parsing
and creating MAVLink packets.

## Generated Code

### General
The generated code is in general used as the following examples illustrates.

```scala
import org.mavlink._

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
val message = Heartbeat(0)

//pack the message into a payload
val (id: Byte, payload: Array[Byte]) = Message.pack(message)

//assemble into packet
val packet = assembler.assemble(id, payload)

//simulate wire transfer
val data = packet.toArray
parser.push(data)
```

The concrete message implementations are generated according to the selected dialect (see section Keys).

### Types
#### Messages
Every message is mapped to a Scala case class, its name converted to CamelCase. The fields of
the case class correspond to the fields defined in the dialect definition (names are converted to camelCase).

#### Fields
Field types are mapped according to the following table

| Definition Type			| Scala Type            |
| ------------------------- | --------------------- |
| int8_t / uint8_t / char	| Byte                  |
| in16_t / uint16_t			| Short                 |
| int32_t / uint32_t		| Int                   |
| int64_t / uint64_t		| Long                  |
| float						| Float                 |
| double					| Double                |
| char[]					| String                |
| &lt;type&gt;[]			| Array[&lt;type&gt;]   |

Remarks:
 1. Since Scala only supports signed integer types, it is up to the client to interpret the values of fields correctly.
 2. Read-only fields such as uint8_t_mavlink_version don't play well with case classes. These fields are therefore treated as writeable by the user. E.g. when creating a standard heartbeat message, the user must add the mavlink version manually.

#### Enums
Enums are mapped to Scala objects, their fields defined as final vals (CamelCase) of type Int.

*Note that since many MAVLink messages that use enums do not define a dependency on them in XML (no 'enum=' attribute), no type safety
can be guaranteed when generating messages.*

## Usage
Add the following to your plugins:

 ```scala
 addSbtPlugin("com.github.jodersky" % "sbt-mavlink" % "0.5.2")`
 ```

Enable the plugin
 ```scala
 enablePlugins(SbtMavlink)
 ```

Set a MAVLink dialect
 ```scala
 mavlinkDialect := baseDirectory.value / "mavlink" / "dialect.xml"
 ```

Compile your project.

## Keys
All keys are defined in ```com.github.jodersky.mavlink.sbt.MavlinkKeys```

 -  ```mavlinkDialect``` - [Setting] - Specifies the location of the MAVLink dialect definition for which to generate Scala code.
 -  ```mavlinkGenerate``` - [Task] - Generates Scala code to interoperate with MAVLink. In a standard project using this plugin this
    task is added to ```sourceGenerators```, i.e. it is automatically called before compiling.

 - ```mavlinkTarget``` - [Setting] - Output directory of generated Scala sources. This should be within ```sourceManaged```.

## Credits
Copyright (c) 2015 Jakob Odersky

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
