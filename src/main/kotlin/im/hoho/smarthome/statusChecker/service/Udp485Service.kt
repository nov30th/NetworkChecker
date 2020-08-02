package im.hoho.smarthome.statusChecker.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.concurrent.thread


class Udp485Service(val ip: String, val port: Int) :ISend {
    private val logger: Logger = LogManager.getLogger(Udp485Service::class.java)

    private val lcdScreen = 12
    var socket = DatagramSocket(65534)

    private val lcdMessagePrefix = "EEB110${"%04x".format(lcdScreen)}{ID}{Content}FFFCFFFF"

    override fun sendButtonStatus(controlId: Int, isStatusNormal: Boolean) {
        val messageContent =
                lcdMessagePrefix
                        .replace("{ID}", "%04x".format(controlId))
                        .replace("{Content}", "%02x".format(if (isStatusNormal) 1 else 0))
        val finalMessage = convertStringToHexToBytes(messageContent)
        sendMessage(finalMessage)
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
        thread { monitorSocket() }
    }

    private fun monitorSocket() {
        var btnStatus = true
        while (true) {
            Thread.sleep(500)
            try {
                btnStatus = !btnStatus
                sendButtonStatus(300, btnStatus)
            } catch (_: Exception) {
                Thread.sleep(10000)
            }
        }
    }

     override fun sendMessage(bytes: ByteArray): Boolean {
        try {
            val packet = DatagramPacket(bytes, bytes.size, InetSocketAddress(ip, port))
            socket.send(packet)
            return true
        } catch (e: Exception) {
            logger.error("error occured when sending message...", e.message)
        }
        return false
    }

}


