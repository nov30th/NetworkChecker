package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import java.io.IOException

import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.*


class TelnetService(type: CheckType, cacheItem: List<EnvCacheItem>) :
        StatusAbstract(type, cacheItem) {


    override fun startup() {
        logger.info("Starting Telnet Service..")
        while (true) {
            Thread.sleep(5000)
            cacheItem.forEach {
                try {
                    logger.debug("telnetting ${it.ip}..")
                    val server = Socket()
                    val address = InetSocketAddress(it.ip, it.port)
                    server.connect(address, it.limitValue)
                    server.close()
                    it.status = 1
                    it.statusMessage = "OK"
                    logger.debug("OK")
                } catch (e: UnknownHostException) {
                    logger.warn("telnet error during [${it.ip}]/${it.name} with UnknownHostException ${e.message}")
                    it.statusMessage = e.message.toString()
                    it.status = 0
                } catch (e: IOException) {
                    logger.warn("telnet error during [${it.ip}]/${it.name} with IOException ${e.message}")
                    it.statusMessage = e.message.toString()
                    it.status = 0
                }
                it.lastUpdate = Date().time
            }
        }
    }
}