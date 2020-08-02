package im.hoho.smarthome.statusChecker.service

interface ISend {
    fun sendMessage(bytes: ByteArray): Boolean
    fun sendButtonStatus(controlId: Int, isStatusNormal: Boolean)
    fun startup()
}