package net.devopssolutions.demo.ws.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BootServer

fun main(args: Array<String>) {
    runApplication<BootServer>(*args)
}
