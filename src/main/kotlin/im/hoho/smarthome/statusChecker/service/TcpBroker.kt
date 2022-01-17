package im.hoho.smarthome.statusChecker.service

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.net.InetSocketAddress
import java.nio.charset.Charset

class TcpBroker(private val ip: String, private val port: Int) {
    private val outputs = mutableListOf<ByteWriteChannel>()
    private val logger: Logger = LogManager.getLogger(MqttClientService::class.java)
    private var whiteList = listOf<String>("192.168.123.165", "127.0.0.1", "localhost")
    private val controlByte = 0xEE
    private val allowedBegin = arrayListOf(0xEE.toByte(), 0xCE.toByte())


    fun startup() {


        prepareConfiguration()

        runBlocking {
            val server = aSocket(ActorSelectorManager(Dispatchers.IO))
                .tcp().bind(InetSocketAddress(ip, port))
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
                            val data = ByteArray(65535)
                            val dataRead = input.readAvailable(data)
                            if (dataRead < 0) {
                                outputs.remove(output)
                                break
                            }
                            if (dataRead < 1) {
                                continue
                            }
                            if (data[0] == controlByte.toByte()) {
                                if (!allowControlPacket)
                                    continue
                            }
                            if (data[0] !in allowedBegin) {
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

    private fun prepareConfiguration() {
        val file = File(System.getProperty("user.dir") + "\\whiteListIp.txt")
        if (file.exists())
            whiteList = file.readLines(Charset.defaultCharset())
        else
            file.writeText(whiteList.joinToString(System.getProperty("line.separator")))
    }
}