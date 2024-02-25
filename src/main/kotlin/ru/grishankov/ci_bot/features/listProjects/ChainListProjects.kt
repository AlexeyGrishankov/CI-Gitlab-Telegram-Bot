package ru.grishankov.ci_bot.features.listProjects

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.columnButtons
import ru.grishankov.ci_bot.database.Project
import ru.grishankov.ci_bot.navigation.ChainNavigation

fun Dispatcher.addChainListProjects() {

    callbackQuery(ChainNavigation.ListProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        val data = transaction { Project.all().toList() }

        if (data.isEmpty()) bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Нет проектов",
            replyMarkup = InlineKeyboardMarkup.createSingleButton(
                InlineKeyboardButton.CallbackData(
                    text = "< Назад",
                    callbackData = ChainNavigation.Menu.name
                )
            )
        )
        else bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Список проектов:",
            replyMarkup = columnButtons(
                data.map {
                    InlineKeyboardButton.CallbackData(
                        text = it.name,
                        callbackData = "${ChainNavigation.DetailProject.name} ${it.id.value}"
                    )
                } + InlineKeyboardButton.CallbackData(
                    text = "< Назад",
                    callbackData = ChainNavigation.Menu.name
                )
            )
        )
    }
}
