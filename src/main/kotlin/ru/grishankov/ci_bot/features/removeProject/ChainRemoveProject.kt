package ru.grishankov.ci_bot.features.removeProject

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.columnButtons
import ru.grishankov.ci_bot.database.Project
import ru.grishankov.ci_bot.database.ProjectAndroidVersions
import ru.grishankov.ci_bot.database.Projects
import ru.grishankov.ci_bot.navigation.ChainNavigation

fun Dispatcher.addChainRemoveProject() {
    callbackQuery(ChainNavigation.RemoveProject.name) {
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
            text = "Удалить проект",
            replyMarkup = columnButtons(
                data.map {
                    InlineKeyboardButton.CallbackData(
                        text = it.name,
                        callbackData = "${ChainNavigation.RemoveSelectProject.name} ${it.id.value}"
                    )
                } + InlineKeyboardButton.CallbackData(
                    text = "< Назад",
                    callbackData = ChainNavigation.Menu.name
                )
            )
        )
    }

    callbackQuery(ChainNavigation.RemoveSelectProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        runCatching {

            val data = callbackQuery.data
                .replace(ChainNavigation.RemoveSelectProject.name, "")
                .trim()
                .toInt()

            transaction {
                Projects.deleteWhere { id eq data }
                ProjectAndroidVersions.deleteWhere { idProject eq data }
            }

            bot.editMessageText(
                chatId = ChatId.fromId(message.chat.id),
                messageId = message.messageId,
                text = "Проект удален",
                replyMarkup = InlineKeyboardMarkup.createSingleButton(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.Menu.name
                    )
                )
            )

        }.getOrElse {
            bot.editMessageText(
                chatId = ChatId.fromId(message.chat.id),
                messageId = message.messageId,
                text = "Не удалось удалить проект :(",
                replyMarkup = InlineKeyboardMarkup.createSingleButton(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.Menu.name
                    )
                )
            )
        }
    }
}
