package ru.grishankov.ci_bot.services

import com.github.kotlintelegrambot.entities.ChatId
import ru.grishankov.ci_bot.actions.TransactionAction

class RuntimeTransaction {
    private val data = HashMap<ChatId, TransactionAction>()

    fun addAction(chatId: ChatId, action: TransactionAction)  {
        data[chatId] = action
    }

    fun getCurrentAction(chatId: ChatId): TransactionAction? {
        return data[chatId]
    }

    fun removeAction(chatId: ChatId) {
        data.remove(chatId)
    }
}
