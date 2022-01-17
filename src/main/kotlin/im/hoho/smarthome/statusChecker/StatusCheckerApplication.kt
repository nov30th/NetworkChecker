package im.hoho.smarthome.statusChecker

import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.service.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@SpringBootApplication
class StatusCheckerApplication {

    val logger: Logger = LogManager.getLogger(StatusCheckerApplication::class.java)

    @Autowired
    lateinit var localCache: LocalCache

    lateinit var pingService: PingService
    lateinit var telnetService: TelnetService
    lateinit var httpsService: HttpsService
    val threads = mutableListOf<Thread>()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<StatusCheckerApplication>(*args)
        }
    }

    @Bean
    fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner { _ ->
            run {
                val userDir = System.getProperty("user.dir")
                logger.info(userDir)
                logger.info("*********** Https://hoho.im **************")
                logger.info("Starting...")
//                val path: String
//                val file = File(this.javaClass.protectionDomain.codeSource.location.path)
//                path = file.path.toString().replace("file:\\", "").replace("statusChecker.jar!\\BOOT-INF\\classes!", "")

                val properties = Properties()
                val propertiesFile = System.getProperty("user.dir") + "\\settings.properties"
                val propFile = File(propertiesFile)
                if (propFile.exists()) {
                    val reader = FileReader(propertiesFile)
                    properties.load(reader)
                } else {
                    properties["mqtt-user"] = "mqtt_username"
                    properties["mqtt-pwd"] = "mqtt_pwd"
                    val fileWriter = FileWriter(propertiesFile)
                    properties.store(fileWriter, "save to properties file")
                }

                val services: List<String>?
                val networkServices = mutableListOf<ISend>()
                val servers = File(System.getProperty("user.dir") + "\\services.txt")
                if (servers.exists()) {
                    services = servers.readLines()
                } else {
                    logger.warn("Empty services file, please edit the services.txt and restart.")
                    servers.writeText(
                        listOf<String>(
                            "TcpBroker,0.0.0.0,9999",
                            "Tcp485Service,127.0.0.1,9999",
                            "MqttClientService,192.168.123.165,1883"
                        ).joinToString(System.getProperty("line.separator")),
                    )
                    exitProcess(-2)
                }

                localCache.readLocalCsv(System.getProperty("user.dir") + "\\networkStatus.csv")

                for (serviceDesc: String in services) {
                    val args = serviceDesc.split(",")
                    val ip = args[1]
                    val port = args[2].toInt()
                    when (args[0]) {
                        "TcpBroker" -> {
                            val service = TcpBroker(ip, port)
                            thread { service.startup() }
                        }
                        "Tcp485Service" -> {
                            val service = Tcp485Service(ip, port)
                            networkServices.add(service)
                            thread { service.startup() }
                        }
                        "MqttClientService" -> {
                            val service = MqttClientService(
                                ip, port, properties["mqtt-user"].toString(), properties["mqtt-pwd"].toString()
                            )
                            networkServices.add(service)
                            thread { service.startup() }
                        }
                    }
                }

                pingService = PingService(
                    networkServices,
                    CheckType.PING,
                    localCache.getCache().filter { it.checkType == CheckType.PING })
                telnetService = TelnetService(
                    networkServices,
                    CheckType.TELNET,
                    localCache.getCache().filter { it.checkType == CheckType.TELNET })
                httpsService = HttpsService(
                    networkServices,
                    CheckType.HTTPS,
                    localCache.getCache().filter { it.checkType == CheckType.HTTPS })


                thread { pingService.startup() }
                thread { telnetService.startup() }
                thread { httpsService.startup() }
            }
        }
    }


}
