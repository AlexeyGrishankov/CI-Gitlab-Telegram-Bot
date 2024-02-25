package ru.grishankov.ci_bot.httpClient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*


val httpClient by lazy {
    HttpClient(CIO) {
        install(Logging) {
            logger = Log()
            level = LogLevel.BODY
        }
    }
}

class Log : Logger {
    override fun log(message: String) {
        println("KTOR")
        println(message)
    }
}
