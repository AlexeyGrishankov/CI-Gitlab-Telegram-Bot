package ru.grishankov.ci_bot.features.menu

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import ru.grishankov.ci_bot.navigation.ChainNavigation

fun Dispatcher.addCommandMenu() {
    command("menu") {
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Hello :)",
            replyMarkup = InlineKeyboardMarkup.createSingleButton(
                InlineKeyboardButton.CallbackData(
                    text = "Открыть меню",
                    callbackData = ChainNavigation.Menu.name
                ),
            )
        )
    }
    callbackQuery(ChainNavigation.Menu.name) {
        val message = callbackQuery.message ?: return@callbackQuery
        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Выберите действие:",
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Список проектов",
                        callbackData = ChainNavigation.ListProject.name
                    ),
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Создать проект",
                        callbackData = ChainNavigation.CreateProject.name
                    ),
                    InlineKeyboardButton.CallbackData(
                        text = "Удалить проект",
                        callbackData = ChainNavigation.RemoveProject.name
                    ),
                ),
            )
        )
    }
}
