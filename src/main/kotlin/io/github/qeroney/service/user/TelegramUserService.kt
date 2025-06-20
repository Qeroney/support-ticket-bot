package io.github.qeroney.service.user

import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.transaction.action.TransactionAction
import io.github.qeroney.model.TelegramUser
import io.github.qeroney.repository.TelegramUserRepository
import io.github.qeroney.service.user.argument.CreateTelegramUser
import org.springframework.stereotype.Service

@Service
class TelegramUserService(
    private val transactional: TransactionAction,
    private val repository: TelegramUserRepository){

    suspend fun upsert(arg: CreateTelegramUser): TelegramUser = transactional {
        (repository.findByChatId(arg.chatId)?.apply {
            email = arg.email
            phone = arg.phone
            fullName = arg.fullName
        } ?: TelegramUser(
            chatId = arg.chatId,
            email = arg.email,
            phone = arg.phone,
            fullName = arg.fullName,
         )).let(repository::save)
    }

    suspend fun getByChatId(chatId: Long) = transactional(readOnly = true) {
        repository.findByChatId(chatId) ?: throw ChatException("Пользователь не найден")
    }
}