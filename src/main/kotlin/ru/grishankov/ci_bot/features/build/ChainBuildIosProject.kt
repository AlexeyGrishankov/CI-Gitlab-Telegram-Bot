package ru.grishankov.ci_bot.features.build

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ru.grishankov.ci_bot.navigation.ChainNavigation

fun Dispatcher.addChainBuildIosProject() {
    callbackQuery(ChainNavigation.BuildIosProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        val arguments = callbackQuery.data
            .replace(ChainNavigation.BuildIosProject.name, "")
            .trim()
            .split(" ")

        val id = arguments.component1()

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Выберите действие:",
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = "${ChainNavigation.DetailProject.name} $id",
                    )
                )
            )
        )
    }
}
