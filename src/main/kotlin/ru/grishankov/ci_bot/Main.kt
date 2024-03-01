package ru.grishankov.ci_bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import ru.grishankov.ci_bot.actions.TransactionAction
import ru.grishankov.ci_bot.config.AppConfig
import ru.grishankov.ci_bot.database.ProjectAndroidVersions
import ru.grishankov.ci_bot.database.Projects
import ru.grishankov.ci_bot.database.WorkingBranches
import ru.grishankov.ci_bot.features.build.addChainBuildAndroidProject
import ru.grishankov.ci_bot.features.build.addChainBuildIosProject
import ru.grishankov.ci_bot.features.build.addMessageBuildBranch
import ru.grishankov.ci_bot.features.createProject.addChainCreateProject
import ru.grishankov.ci_bot.features.createProject.addMessageCreateProject
import ru.grishankov.ci_bot.features.listProjects.addChainListProjects
import ru.grishankov.ci_bot.features.menu.addCommandMenu
import ru.grishankov.ci_bot.features.projectDetail.addChainDetailProject
import ru.grishankov.ci_bot.features.removeProject.addChainRemoveProject
import ru.grishankov.ci_bot.features.settingsProject.addChainSettingsProject
import ru.grishankov.ci_bot.features.settingsProject.addMessageChangeWorkingBranch
import ru.grishankov.ci_bot.services.RuntimeTransaction

val DATABASE by lazy {
    val fileUrl = "data/ci_hld.db"
    Database.connect(
        "jdbc:sqlite:$fileUrl",
        "org.sqlite.JDBC",
    )
}

fun main() {
    val transactions = RuntimeTransaction()

    transaction(DATABASE) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(
            Projects,
            ProjectAndroidVersions,
            WorkingBranches,
        )
    }

    val bot = bot {
        token = AppConfig.configuration.botToken

        dispatch {
            command("id") {
                bot.sendMessage(ChatId.fromId(message.chat.id), message.chat.id.toString())
            }
            addCommandMenu()
            addChainListProjects()
            addChainCreateProject(transactions)
            addChainRemoveProject()
            addChainDetailProject()
            addChainBuildAndroidProject(transactions)
            addChainBuildIosProject()
            addChainSettingsProject(transactions)

            message {

                val action = transactions
                    .getCurrentAction(ChatId.fromId(message.chat.id)) ?: return@message

                when (action) {
                    is TransactionAction.None ->
                        transactions.removeAction(ChatId.fromId(message.chat.id))

                    is TransactionAction.CreateProject ->
                        addMessageCreateProject(transactions)

                    is TransactionAction.BuildBranch ->
                        addMessageBuildBranch(action.projectId, transactions)

                    is TransactionAction.WorkBranch ->
                        addMessageChangeWorkingBranch(transactions, action.branchId, action.type)
                }
            }
        }
    }
    bot.startPolling()
}

fun columnButtons(buttons: List<InlineKeyboardButton.CallbackData>): ReplyMarkup {
    return InlineKeyboardMarkup.create(buttons.chunked(1))
}