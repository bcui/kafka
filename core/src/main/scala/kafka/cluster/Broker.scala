/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.cluster

import kafka.utils.Utils._
import kafka.api.ApiUtils._
import java.nio.ByteBuffer
import kafka.common.BrokerNotAvailableException

/**
 * A Kafka broker
 */
private[kafka] object Broker {

  def createBroker(id: Int, brokerInfoString: String): Broker = {
    if(brokerInfoString == null)
      throw new BrokerNotAvailableException("Broker id %s does not exist".format(id))
    val brokerInfo = brokerInfoString.split(":")
    new Broker(id, brokerInfo(0), brokerInfo(1), brokerInfo(2).toInt)
  }

  def readFrom(buffer: ByteBuffer): Broker = {
    val id = buffer.getInt
    val creatorId = readShortString(buffer)
    val host = readShortString(buffer)
    val port = buffer.getInt
    new Broker(id, creatorId, host, port)
  }
}

private[kafka] case class Broker(val id: Int, val creatorId: String, val host: String, val port: Int) {
  
  override def toString(): String = new String("id:" + id + ",creatorId:" + creatorId + ",host:" + host + ",port:" + port)

  def getZKString(): String = new String(creatorId + ":" + host + ":" + port)

  def writeTo(buffer: ByteBuffer) {
    buffer.putInt(id)
    writeShortString(buffer, creatorId)
    writeShortString(buffer, host)
    buffer.putInt(port)
  }

  def sizeInBytes: Int = {
    val size = shortStringLength(creatorId) + shortStringLength(host) /* host name */ + 4 /* port */ + 4 /* broker id*/
    debug("Size of broker info = " + size)
    size
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case n: Broker => id == n.id && host == n.host && port == n.port
      case _ => false
    }
  }
  
  override def hashCode(): Int = hashcode(id, host, port)
  
}
