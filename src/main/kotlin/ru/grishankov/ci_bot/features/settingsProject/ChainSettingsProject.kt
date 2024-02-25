package ru.grishankov.ci_bot.features.settingsProject

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
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

fun Dispatcher.addChainSettingsProject(transaction: RuntimeTransaction) {
    callbackQuery(ChainNavigation.SettingsProject.name) {
        val message = callbackQuery.message ?: return@callbackQuery

        val id = callbackQuery.data
            .replace(ChainNavigation.SettingsProject.name, "")
            .trim()
            .toIntOrNull() ?: return@callbackQuery

        val project = transaction { Project[id] }

        bot.editMessageText(
            chatId = ChatId.fromId(message.chat.id),
            messageId = message.messageId,
            text = "Настройки проекта: <b>${project.name}</b>",
            parseMode = ParseMode.HTML,
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Изменить ветку для продакшена",
                        callbackData = ChainNavigation.SettingsProdBranch.name + " $id"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Изменить ветку для тестирования",
                        callbackData = ChainNavigation.SettingsDemoBranch.name + " $id"
                    )
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.DetailProject.name + " $id"
                    )
                )
            )
        )
    }
    callbackQuery(ChainNavigation.SettingsProdBranch.name) {
        handleBranch(ChainNavigation.SettingsProdBranch.name) { id, branch ->
            transaction.addAction(id, TransactionAction.WorkBranch(branch, BranchType.PROD))
        }
    }
    callbackQuery(ChainNavigation.SettingsDemoBranch.name) {
        handleBranch(ChainNavigation.SettingsDemoBranch.name) { id, branch ->
            transaction.addAction(id, TransactionAction.WorkBranch(branch, BranchType.DEMO))
        }
    }
}

private fun CallbackQueryHandlerEnvironment.handleBranch(replace: String, action: (ChatId.Id, Int) -> Unit) {
    val message = callbackQuery.message ?: return

    val id = callbackQuery.data
        .replace(replace, "")
        .trim()
        .toIntOrNull() ?: return

    val branches = transaction {
        val project = Project[id]
        val branch = WorkingBranch.all().find { it.project.id.value == project.id.value }
        val idBranch = branch?.id ?: WorkingBranches.insertAndGetId { it[idProject] = project.id }
        WorkingBranch[idBranch]
    }
    val chatId = ChatId.fromId(message.chat.id)
    bot.deleteMessage(chatId, message.messageId)
    bot.sendMessage(
        chatId = chatId,
        text = "Введите название ветки проекта. Пример: <code>feature/id-222</code>",
        parseMode = ParseMode.HTML,
    ).fold(
        ifSuccess = { action(chatId, branches.id.value) },
        ifError = { println(it) }
    )
}

enum class BranchType {
    PROD, DEMO
}
