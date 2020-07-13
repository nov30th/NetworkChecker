package im.hoho.smarthome.statusChecker

import com.fasterxml.jackson.databind.ObjectMapper
import im.hoho.smarthome.statusChecker.model.Category
import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File


@Service
class LocalCache {
    val logger: Logger = LogManager.getLogger(StatusCheckerApplication::class.java)

    private val cache: MutableList<EnvCacheItem> = mutableListOf()

    @Autowired
    lateinit var mapper: ObjectMapper

    fun readLocalCsv(fileName: String) {
        logger.info("Loading ${File(fileName).absolutePath} CSV file to program...")
        var skipFirstLine = true
        File(fileName).forEachLine fileloading@{
            if (skipFirstLine) {
                skipFirstLine = false
                return@fileloading
            }
            cache.add(convertToItem(it))
        }
        logger.info("loaded ${cache.size} items into local cache.")
    }

    fun getCache(): List<EnvCacheItem> {
        return cache.toList()
    }

    private fun convertToItem(param: String): EnvCacheItem {
        val params = param.split(",")
        val item = EnvCacheItem()
        item.ip = params[0]
        item.mac = params[1].replace(":", "")
        item.port = params[2].toInt()
        item.name = params[3]
        item.checkType = when (params[4]) {
            "IP" -> CheckType.PING
            "HTTP" -> CheckType.HTTP
            "HTTPS" -> CheckType.HTTPS
            "TELNET" -> CheckType.TELNET
            else -> CheckType.PING
        }
        item.category = when (params[5]) {
            "IP" -> Category.IP
            "DOMAIN" -> Category.DOMAIN
            else -> Category.IP
        }
        item.networkNum = params[6].toInt()
        item.checkInv = params[7].toInt()
        item.limitValue = params[8].toInt()
        item.enabled = when (params[9].toInt()) {
            1 -> true
            else -> false
        }
        logger.info("Converting ${mapper.writeValueAsString(item)}")
        return item
    }

}