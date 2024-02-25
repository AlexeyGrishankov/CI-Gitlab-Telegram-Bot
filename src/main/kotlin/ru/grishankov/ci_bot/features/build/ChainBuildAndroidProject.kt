package ru.grishankov.ci_bot.features.build

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.actions.TransactionAction
import ru.grishankov.ci_bot.database.Project
import ru.grishankov.ci_bot.database.WorkingBranch
import ru.grishankov.ci_bot.database.WorkingBranches
import ru.grishankov.ci_bot.navigation.ChainNavigation
import ru.grishankov.ci_bot.services.RuntimeTransaction

fun Dispatcher.addChainBuildAndroidProject(transactions: RuntimeTransaction) {
    callbackQuery(ChainNavigation.BuildAndroidProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        val arguments = callbackQuery.data
            .replace(ChainNavigation.BuildAndroidProject.name, "")
            .trim()
            .split(" ")

        val id = arguments.component1().trim().toInt()

        val branches = transaction {
            val project = Project[id]
            val branch = WorkingBranch.all().find { it.project.id.value == project.id.value }
            val idBranch = branch?.id ?: WorkingBranches.insertAndGetId { it[idProject] = project.id }
            WorkingBranch[idBranch]
        }

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Выберите действие:",
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Сборка для продакшена",
                        callbackData = "${ChainNavigation.AndroidBuild.name} $id ${branches.prodBranch}",
                    ),
                    InlineKeyboardButton.CallbackData(
                        text = "Сборка для тестирования",
                        callbackData = "${ChainNavigation.AndroidBuild.name} $id ${branches.demoBranch}",
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Сборка ветки",
                        callbackData = "${ChainNavigation.AndroidBuild.name} $id",
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = "${ChainNavigation.DetailProject.name} $id",
                    )
                )
            )
        )
    }
    callbackQuery(ChainNavigation.AndroidBuild.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        val arguments = callbackQuery.data
            .replace(ChainNavigation.AndroidBuild.name, "")
            .trim()
            .split(" ")

        val id = arguments.component1().toInt()
        val branch = runCatching { arguments.component2() }.getOrNull()

        val chatId = ChatId.fromId(message.chat.id)

        if (branch.isNullOrEmpty()) {
            bot.deleteMessage(
                chatId = chatId,
                messageId = message.messageId,
            )
            bot.sendMessage(
                chatId = chatId,
                text = "Введите название ветки проекта. Пример: <code>feature/id-222</code>",
                parseMode = ParseMode.HTML,
            ).fold(
                ifSuccess = { transactions.addAction(ChatId.fromId(it.chat.id), TransactionAction.BuildBranch(id)) },
                ifError = { println(it) }
            )
        } else {
            buildBranchAndroid(bot, chatId, message.messageId, branch, id)
        }
    }
}
