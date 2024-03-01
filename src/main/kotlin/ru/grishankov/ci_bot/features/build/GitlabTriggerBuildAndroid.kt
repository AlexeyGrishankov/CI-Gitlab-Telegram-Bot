package ru.grishankov.ci_bot.features.build

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.config.AppConfig
import ru.grishankov.ci_bot.database.Project
import ru.grishankov.ci_bot.database.ProjectAndroidVersion
import ru.grishankov.ci_bot.httpClient.httpClient

suspend fun buildBranchAndroid(bot: Bot, chatId: ChatId.Id, messageId: Long?, branch: String, projectId: Int) {
    val project = transaction {
        Project[projectId] to ProjectAndroidVersion.all().last { it.project.id.value == projectId }
    }
    transaction { project.second.increment += 1 }
    messageId?.also { bot.deleteMessage(chatId, messageId) }
    handleTriggerBranch(bot, project, chatId, branch)
}

private suspend fun gitlabTrigger(project: Project, version: Int, chatId: Long, branch: String): HttpResponse? {
    return runCatching {
        httpClient.post("https://${AppConfig.configuration.gitlabLink}/api/v4/projects/${project.gitId}/ref/$branch/trigger/pipeline") {
            parameter("token", project.token)
            parameter("variables[TOKEN_TELEGRAM_BOT]", AppConfig.configuration.botToken)
            parameter("variables[TOKEN_TELEGRAM_CHAT]", chatId)
            parameter("variables[VERSION_CODE]", version)
//            parameter("variables[VERSION_NAME_CODE]", version)
            parameter("variables[PROJECT_NAME]", project.name)
        }
    }.getOrNull()
}

private suspend fun handleTriggerBranch(
    bot: Bot,
    project: Pair<Project, ProjectAndroidVersion>,
    chatId: ChatId.Id,
    branch: String
) {
    val response = gitlabTrigger(project.first, project.second.increment, chatId.id, branch)
    when (response?.status) {
        HttpStatusCode.Created -> bot.sendMessage(
            chatId = chatId,
            text = "${project.first.name} - Запущен пайплайн на ветку $branch"
        )

        HttpStatusCode.BadRequest -> bot.sendMessage(
            chatId = chatId,
            text = "Ветка не найдена"
        )

        else -> bot.sendMessage(
            chatId = chatId,
            text = "Не удалось запустить пайплайн"
        )
    }
}
