package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.model.EnvCacheItem

interface ISend {
    fun sendMessage(bytes: ByteArray): Boolean
    fun sendButtonStatus(controlId: Int, isStatusNormal: Boolean)
    fun sendButtonStatus(cacheItem: EnvCacheItem)
    fun startup()
}