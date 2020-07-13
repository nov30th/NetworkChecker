package im.hoho.smarthome.statusChecker

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.net.InetAddress

@SpringBootTest
class StatusCheckerApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun pingTest() {

        var inet: InetAddress
        inet = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
        println("Sending Ping Request to $inet")
        println(if (inet.isReachable(5000)) "Host is reachable" else "Host is NOT reachable")

        inet = InetAddress.getByAddress(byteArrayOf(173.toByte(), 194.toByte(), 32, 38))
        println("Sending Ping Request to $inet")
        println(if (inet.isReachable(5000)) "Host is reachable" else "Host is NOT reachable")
    }


}
