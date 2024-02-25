package ru.grishankov.ci_bot.features.createProject

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.columnButtons
import ru.grishankov.ci_bot.database.Project
import ru.grishankov.ci_bot.database.ProjectAndroidVersions
import ru.grishankov.ci_bot.database.Projects
import ru.grishankov.ci_bot.database.WorkingBranches
import ru.grishankov.ci_bot.navigation.ChainNavigation
import ru.grishankov.ci_bot.services.RuntimeTransaction

fun MessageHandlerEnvironment.addMessageCreateProject(transactions: RuntimeTransaction) {
    runCatching {
        transactions.removeAction(ChatId.fromId(message.chat.id))
        val newProject = message.text?.split(" ") ?: throw RuntimeException()

        val created = transaction {
            val id = Projects.insertAndGetId {
                it[name] = newProject.component1().trim()
                it[token] = newProject.component2().trim()
                it[gitId] = newProject.component3().trim().toInt()
            }
            ProjectAndroidVersions.insert {
                it[increment] = 1
                it[idProject] = id
            }
            WorkingBranches.insert {
                it[prodBranch] = "main"
                it[demoBranch] = "develop"
                it[idProject] = id
            }
            Project[id]
        }

        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Создан новый проект ${created.name}",
            replyMarkup = columnButtons(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Добавить еще",
                        callbackData = ChainNavigation.CreateProject.name
                    ),
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
            text = "Не удалось создать проект",
            replyMarkup = columnButtons(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Попробовать снова",
                        callbackData = ChainNavigation.CreateProject.name
                    ),
                    InlineKeyboardButton.CallbackData(
                        text = "< Назад",
                        callbackData = ChainNavigation.Menu.name
                    )
                )
            )
        )
    }
}
