package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class Tcp485Service(val ip: String, val port: Int) : ISend {
    private val logger: Logger = LogManager.getLogger(Tcp485Service::class.java)

    private val socketClient = Socket()
    private val lcdScreen = 12

    private val lcdMessagePrefix = "EEB110${"%04x".format(lcdScreen)}{ID}{Content}FFFCFFFF"

    override fun sendButtonStatus(controlId: Int, isStatusNormal: Boolean) {
        val messageContent =
                lcdMessagePrefix
                        .replace("{ID}", "%04x".format(controlId))
                        .replace("{Content}", "%02x".format(if (isStatusNormal) 1 else 0))
        val finalMessage = convertStringToHexToBytes(messageContent)
        sendMessage(finalMessage)
    }

    override fun sendButtonStatus(cacheItem: EnvCacheItem) {
        sendButtonStatus(cacheItem.lcdButton, cacheItem.status == 1)
    }

    fun sendText(controlId: Int, textContent: String) {
        val messageContent =
                lcdMessagePrefix
                        .replace("{ID}", "%04x".format(controlId))
                        .replace("{Content}", ",")
        val prefixBytes = messageContent.split(",")
        val textContentBytes = textContent.toByteArray()
        val finalBytes = convertStringToHexToBytes(prefixBytes[0]) +
                textContentBytes +
                convertStringToHexToBytes(prefixBytes[1])
        sendMessage(finalBytes)
    }

    private fun convertStringToHexToBytes(content: String): ByteArray {
        return content.chunked(2).map {
            ((Character.digit(it[0], 16) shl 4)
                    + Character.digit(it[1], 16)).toByte()
        }.toByteArray()
    }

    override fun startup() {
        connectTo485()
        thread { monitorSocket() }
    }

    private fun monitorSocket() {
        var btnStatus = true
        while (true) {
            Thread.sleep(500)
            try {
                btnStatus = !btnStatus
                if (!socketClient.isConnected) {
                    Thread.sleep(5000)
                    logger.warn("Reconnecting...")
                    connectTo485()
                }
                if (btnStatus){
                    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    val currentDate = sdf.format(Date())
                    sendText(2,currentDate)
                }
                sendButtonStatus(300, btnStatus)
            } catch (_: Exception) {

            }
        }
    }

    override fun sendMessage(bytes: ByteArray): Boolean {
        try {
            if (socketClient.isConnected) {
                val outputStream = DataOutputStream(socketClient.getOutputStream())
                outputStream.write(bytes)
                outputStream.flush()
                return true
            }
        } catch (e: Exception) {
            logger.error("error occured when sending message...", e.message)
        }
        return false
    }

    private fun connectTo485() {
        try {
            val address = InetSocketAddress(ip, port)
            socketClient.connect(address, 5000)
            logger.info("Connected with $ip:$port")
        } catch (e: UnknownHostException) {
            logger.error("connecting 485 error during [${ip}]/${port} with UnknownHostException ${e.message}")
        } catch (e: IOException) {
            logger.error("connecting 485 error during [${ip}]/${port} with IOException ${e.message}")
        }
    }
}


