package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.StatusCheckerApplication
import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.InetAddress


abstract class StatusAbstract(val type: CheckType, val cacheItem: List<EnvCacheItem>) : IStatus {
//    lateinit var type: CheckType
//    lateinit var cache: List<EnvCacheItem>
//    lateinit var callback: (name: String, result: String, status: Int) -> Int

    val logger: Logger = LogManager.getLogger(StatusCheckerApplication::class.java)


    override fun startup() {
        TODO("Not yet implemented")
    }

    fun pingTest() {

        var inet: InetAddress
        inet = InetAddress.getByName("127.0.0.1")
        println("Sending Ping Request to $inet")
        println(if (inet.isReachable(5000)) "Host is reachable" else "Host is NOT reachable")

        inet = InetAddress.getByAddress(byteArrayOf(173.toByte(), 194.toByte(), 32, 38))
        println("Sending Ping Request to $inet")
        println(if (inet.isReachable(5000)) "Host is reachable" else "Host is NOT reachable")
    }

}