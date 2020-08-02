package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import java.net.InetAddress
import java.util.*

class PingService(iSend: ISend, type: CheckType, cacheItem: List<EnvCacheItem>) :
        StatusAbstract(iSend, type, cacheItem) {

    override fun startup() {
        logger.info("Starting Ping Service..")
        while (true) {
            Thread.sleep(10000)
            cacheItem.forEach {
                try {
                    logger.debug("ping dest ${it.ip}..")
                    val inet = InetAddress.getByName(it.ip)
                    when (inet.isReachable(it.limitValue)) {
                        true -> {
                            it.status = 1
                            it.statusMessage = "OK"
                            logger.debug("OK")
                        }
                        else -> {
                            it.status = 0
                            it.statusMessage = "NOT reachable"
                            logger.warn("[${it.ip}]/${it.name} is NOT reachable")
                        }
                    }
                } catch (ex: Exception) {
                    logger.error("Ping error during [${it.ip}]/${it.name} with ${ex.message}")
                    it.statusMessage = ex.message.toString()
                    it.status = 0
                }
                it.lastUpdate = Date().time
                iSend.sendButtonStatus(it.lcdButton, it.status == 1)
            }
        }
    }
}