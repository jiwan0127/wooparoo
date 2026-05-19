package org.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
class WooparooApplication

fun main(args: Array<String>) {
    runApplication<WooparooApplication>(*args)
}
