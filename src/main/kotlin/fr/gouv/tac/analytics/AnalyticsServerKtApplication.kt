package fr.gouv.tac.analytics

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AnalyticsServerKtApplication

fun main(args: Array<String>) {
    runApplication<AnalyticsServerKtApplication>(*args)
}
