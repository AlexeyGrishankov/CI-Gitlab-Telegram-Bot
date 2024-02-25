package ru.grishankov.ci_bot.features.settingsProject

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.columnButtons
import ru.grishankov.ci_bot.database.WorkingBranch
import ru.grishankov.ci_bot.navigation.ChainNavigation
import ru.grishankov.ci_bot.services.RuntimeTransaction

fun MessageHandlerEnvironment.addMessageChangeWorkingBranch(transactions: RuntimeTransaction, branchId: Int, type: BranchType) {
    runCatching {
        transactions.removeAction(ChatId.fromId(message.chat.id))
        val newBranch = message.text?.trim() ?: throw RuntimeException()

        val workBranch = transaction {
            val branch = WorkingBranch[branchId]
            when(type) {
                BranchType.PROD -> branch.prodBranch = newBranch
                BranchType.DEMO -> branch.demoBranch = newBranch
            }
            branch
        }
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = when(type) {
                BranchType.PROD -> "Ветка для продакшена изменена на ${workBranch.prodBranch}"
                BranchType.DEMO -> "Ветка для тестирования изменена на ${workBranch.demoBranch}"
            },
            replyMarkup = columnButtons(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.Menu.name
                    )
                )
            )
        )
    }.getOrElse {
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Не удалось изменить ветку",
            replyMarkup = columnButtons(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.Menu.name
                    )
                )
            )
        )
    }
}
