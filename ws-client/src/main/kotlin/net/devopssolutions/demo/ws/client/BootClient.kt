package net.devopssolutions.demo.ws.client

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class BootClient

fun main(args: Array<String>) {
    SpringApplicationBuilder(BootClient::class.java).web(WebApplicationType.NONE).run(*args)

    Thread.currentThread().join()
}
