package im.hoho.smarthome.statusChecker

import im.hoho.smarthome.statusChecker.model.CheckType
import im.hoho.smarthome.statusChecker.service.PingService
import im.hoho.smarthome.statusChecker.service.TelnetService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import kotlin.concurrent.thread

@SpringBootApplication
class StatusCheckerApplication {

    val logger: Logger = LogManager.getLogger(StatusCheckerApplication::class.java)

    @Autowired
    lateinit var localCache: LocalCache

    lateinit var pingService: PingService
    lateinit var telnetService: TelnetService

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<StatusCheckerApplication>(*args)
        }
    }

    @Bean
    open fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner { args ->
            run {
                logger.info("*********** Https://hoho.im **************");
                logger.info("Starting...");
                localCache.readLocalCsv("src/main/resources/test.csv")
                pingService = PingService(CheckType.PING, localCache.getCache().filter { it.checkType == CheckType.PING })
                telnetService = TelnetService(CheckType.TELNET, localCache.getCache().filter { it.checkType == CheckType.TELNET })

                thread { pingService.startup() }
                thread { telnetService.startup() }
            };
        }
    }


}
