package im.hoho.smarthome.statusChecker.model

import java.util.*

data class EnvCacheItem(
        var enabled: Boolean = false,
        var category: Category = Category.IP,
        var name: String = "",
        var ip: String = "",
        var mac: String = "",
        var port: Int = 0,
        var checkType: CheckType = CheckType.PING,
        var networkNum: Int = 0,
//    var checkInv = 0
        var limitValue: Int = 100,
        var lcdText: Int = 0,
        var lcdButton: Int = 0,

        var status: Int = -1,
        var lastUpdate: Long = Date().time,
        var statusMessage: String = "",
        var statusValue: String = ""
) {


}