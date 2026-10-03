package org.example.reaitests

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ReAiTestsApplication

fun main(args: Array<String>) {
    runApplication<ReAiTestsApplication>(*args)
}
