package im.hoho.smarthome.statusChecker.service

import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.model.EnvCacheItem
import java.io.IOException
import java.net.URL
import java.net.UnknownHostException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*


class HttpsService(iSend: ISend, type: CheckType, cacheItem: List<EnvCacheItem>) :
        StatusAbstract(iSend, type, cacheItem) {

    private fun createSocketFactory(protocols: List<String>) =
            SSLContext.getInstance(protocols[0]).apply {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                })
                init(null, trustAllCerts, SecureRandom())
            }.socketFactory

    override fun startup() {
        logger.info("Starting HTTPS Service..")
        while (true) {
            Thread.sleep(30000)
            cacheItem.forEach {
                try {
                    logger.debug("https to ${it.ip}..")
                    val urlPath = it.ip
                    var responseCode = 0

                    (URL(urlPath).openConnection() as HttpsURLConnection).apply {
                        sslSocketFactory = createSocketFactory(listOf("TLSv1.2"))
                        hostnameVerifier = HostnameVerifier { _, _ -> true }
                        readTimeout = 5_000
                        connect()
                        responseCode = getResponseCode()
                    }
                    if (responseCode == it.port) {
                        it.status = 1
                        it.statusMessage = "OK"
                        logger.debug("OK")
                    } else {
                        it.statusMessage = "https error during ${it.ip} response code ${responseCode}"
                        it.status = 0
                        logger.warn("https error during ${it.ip} response code ${responseCode}\"")
                    }
                    logger.debug("OK")
                } catch (e: UnknownHostException) {
                    logger.warn("https error during [${it.ip}] of ${it.name} with UnknownHostException ${e.message}")
                    it.statusMessage = e.message.toString()
                    it.status = 0
                } catch (e: IOException) {
                    logger.warn("https error during [${it.ip}] of ${it.name} with IOException ${e.message}")
                    it.statusMessage = e.message.toString()
                    it.status = 0
                }
                it.lastUpdate = Date().time
                iSend.sendButtonStatus(it.lcdButton, it.status == 1)
            }
        }
    }
}