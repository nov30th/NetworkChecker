package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import java.net.InetAddress

class PingService(type: CheckType, cacheItem: List<EnvCacheItem>) :
        StatusAbstract(type, cacheItem) {

    override fun startup() {
        while (true) {
            Thread.sleep(5000)
            cacheItem.forEach {
                try {
                    val inet = InetAddress.getByName(it.ip)
                    println("Sending Ping Request to $inet")
                    println(if (inet.isReachable(5000)) "${it.ip} Host is reachable" else "${it.ip}Host is NOT reachable")
                } catch (ex: Exception) {

                }
            }
        }
    }
}