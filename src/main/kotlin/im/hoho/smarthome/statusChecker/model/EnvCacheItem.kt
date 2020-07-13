package im.hoho.smarthome.statusChecker.model

class EnvCacheItem {
    var enabled = false
    var category = Category.IP
    var name = ""
    var ip = ""
    var mac = ""
    var port = 0
    var checkType = CheckType.PING
    var networkNum = 0
    var checkInv = 0
    var limitValue = 100
}