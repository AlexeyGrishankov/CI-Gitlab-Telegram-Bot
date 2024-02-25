package ru.grishankov.ci_bot.features.createProject

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import ru.grishankov.ci_bot.actions.TransactionAction
import ru.grishankov.ci_bot.navigation.ChainNavigation
import ru.grishankov.ci_bot.services.RuntimeTransaction

fun Dispatcher.addChainCreateProject(transactions: RuntimeTransaction) {
    callbackQuery(ChainNavigation.CreateProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        bot.deleteMessage(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
        )
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Введите название проекта, его токен и id проекта для подключения. Пример: <code>Название 123qg145 213</code>",
            parseMode = ParseMode.HTML,
        ).fold(
            ifSuccess = { transactions.addAction(ChatId.fromId(it.chat.id), TransactionAction.CreateProject) },
            ifError = { println(it) }
        )
    }
}
