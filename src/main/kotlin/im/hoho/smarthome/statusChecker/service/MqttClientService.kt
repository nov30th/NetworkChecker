package im.hoho.smarthome.statusChecker.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import kotlin.concurrent.thread

/**
 * This MQTT Service is for HA.
 */
class MqttClientService(val ip: String, val port: Int, val username: String, val password: String) : ISend {
    private val logger: Logger = LogManager.getLogger(MqttClientService::class.java)
    val mapper = ObjectMapper()
    private val lcdScreen = 12
    var client = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(ip)
            .serverPort(port)
            .buildBlocking()

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

    override fun sendButtonStatus(cacheItem: EnvCacheItem) {
        try {
            client.publishWith().topic("homeassistant/sensor/hoho_status_${cacheItem.name.replace(" ", "")}/config")
                    .retain(false)
                    .qos(MqttQos.AT_MOST_ONCE)
                    .payload(
                            ("{\"name\": \"${cacheItem.name.replace(" ", "")} Status\"," +
                                    " \"state_topic\": \"homeassistant/sensor/hoho_status_${cacheItem.name.replace(" ", "")}/state\"}")
                                    .toByteArray()).send();
            client.publishWith().topic("homeassistant/sensor/hoho_status_" +
                    "${cacheItem.name.replace(" ", "")}/state")
                    .retain(true)
                    .qos(MqttQos.AT_MOST_ONCE)
                    .payload(if (cacheItem.status == 1) "on".toByteArray() else "off".toByteArray()).send();
//        client.publishWith().topic("homeassistant/sensor/hoho_status_" +
//                "${cacheItem.name.replace(" ", "")}/topic")
//                .retain(true)
//                .qos(MqttQos.AT_MOST_ONCE)
//                .payload("homeassistant/sensor/hoho_status_${cacheItem.name.replace(" ", "")}/state".toByteArray()).send();
        } catch (e: Exception) {
            logger.error("error occurred when sendButtonStatus message...", e.message)
        }
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


        while (true) {
            try {
                if (client.state != MqttClientState.CONNECTED)
                    logger.info("Connecting to MQTT server...")
                client.connectWith().simpleAuth()
                        .username(username)
                        .password(password.toByteArray())
                        .applySimpleAuth()
                        .willPublish()
                        .topic("homeassistant/sensor/hoho_status_boss/state")
                        .qos(MqttQos.AT_MOST_ONCE)
                        .payload("unavailable".toByteArray())
                        .retain(true)
                        .messageExpiryInterval(60)
//                        .delayInterval(10)
                        .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                        .contentType("text/plain")
                        .responseTopic("response/topic")
                        .correlationData("correlationData".toByteArray())
                        .applyWillPublish()
                        .send()
                client.publishWith().topic("homeassistant/sensor/hoho_status_boss/state")
                        .messageExpiryInterval(60)
                        .retain(true)
                        .qos(MqttQos.AT_MOST_ONCE)
                        .payload("on".toByteArray()).send();
                Thread.sleep(5000)
            } catch (_: Exception) {
                Thread.sleep(10000)
            }
        }
    }

    override fun sendMessage(bytes: ByteArray): Boolean {
        try {
            client.publishWith().topic("test/topic").qos(MqttQos.AT_LEAST_ONCE)
                    .messageExpiryInterval(30)
                    .retain(true)
                    .payload("1".toByteArray()).send();
            return true
        } catch (e: Exception) {
            logger.error("error occurred when sending message...", e.message)
        }
        return false
    }

}


