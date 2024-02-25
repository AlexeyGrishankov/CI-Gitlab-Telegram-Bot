package ru.grishankov.ci_bot.features.build

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import ru.grishankov.ci_bot.services.RuntimeTransaction

suspend fun MessageHandlerEnvironment.addMessageBuildBranch(projectId: Int, transactions: RuntimeTransaction) {
    runCatching {
        val branch = message.text ?: throw RuntimeException()
        val chatId = ChatId.fromId(message.chat.id)
        transactions.removeAction(chatId)
        buildBranchAndroid(bot, chatId, null, branch, projectId)
    }
}
