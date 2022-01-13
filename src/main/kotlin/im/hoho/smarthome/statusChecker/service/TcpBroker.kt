package im.hoho.smarthome.statusChecker.service

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.InetSocketAddress

class TcpBroker {
    private val outputs = mutableListOf<ByteWriteChannel>()
    private val logger: Logger = LogManager.getLogger(MqttClientService::class.java)
    private val whiteList = listOf<String>("192.168.123.165","127.0.0.1")
    private val controlByte = 0xEE

    fun startTcpBus(port: Int) {
        runBlocking {
            val server = aSocket(ActorSelectorManager(Dispatchers.IO))
                .tcp().bind(InetSocketAddress("0.0.0.0", port))
            println("Started TCP BUS Server At ${server.localAddress}")

            while (true) {
                val socket = server.accept()

                launch {
                    println("Socket accepted: ${socket.remoteAddress}")
                    //The Asocket likes a shit which can not find any doc to get the remote ip directly -- Vincent.Q
                    val remoteAddressIP = socket.remoteAddress.toString().split("/")[1].split(":")[0]
                    val allowControlPacket = whiteList.contains(remoteAddressIP)
                    val input = socket.openReadChannel()
                    val output = socket.openWriteChannel(autoFlush = true)
                    outputs.add(output)

                    try {
                        while (true) {
                            val data = ByteArray(1000)
                            val dataRead = input.readAvailable(data)
                            if (dataRead < 0) {
                                outputs.remove(output)
                                break
                            }
                            if (data[0] == controlByte.toByte()) {
                                if (!allowControlPacket)
                                    continue
                            }
//                            logger.info("dataread:$dataRead")
                            for (clientOutput in outputs.toList()) {
                                if (clientOutput != output) {
                                    clientOutput.writeFully(data, 0, dataRead)
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        logger.error(e)
                        outputs.remove(output)
                    }
                }
            }
        }
    }
}