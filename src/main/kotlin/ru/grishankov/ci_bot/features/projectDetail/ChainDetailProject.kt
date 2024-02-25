package ru.grishankov.ci_bot.features.projectDetail

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.database.Project
import ru.grishankov.ci_bot.navigation.ChainNavigation
import ru.grishankov.ci_bot.services.RuntimeTransaction

fun Dispatcher.addChainDetailProject() {
    callbackQuery(ChainNavigation.DetailProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        val id = callbackQuery.data
            .replace(ChainNavigation.DetailProject.name, "")
            .trim()
            .toIntOrNull() ?: return@callbackQuery

        val project = transaction {
            Project[id]
        }

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Название проекта: <b>${project.name}</b>",
            parseMode = ParseMode.HTML,
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Собрать Android сборку",
                        callbackData = ChainNavigation.BuildAndroidProject.name + " $id"
                    ),
                    InlineKeyboardButton.CallbackData(
                        text = "Собрать iOS сборку",
                        callbackData = ChainNavigation.BuildIosProject.name + " $id"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Настройка проекта",
                        callbackData = ChainNavigation.SettingsProject.name +  " $id"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.ListProject.name
                    )
                )
            )
        )
    }
}
