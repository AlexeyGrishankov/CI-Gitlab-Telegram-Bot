package ru.grishankov.ci_bot.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object AppConfig {
    val configuration: Config by lazy {
        val fileCfg = File("config")
        fileCfg.mkdirs()
        val jsonFile = File(fileCfg, "settings.json")
        Json.decodeFromString<Config>(jsonFile.readText())
    }
}

@Serializable
data class Config(
    @SerialName("botToken") val botToken: String,
    @SerialName("gitlabLink") val gitlabLink: String,
)
