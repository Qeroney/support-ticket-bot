package io.github.qeroney.repository

import io.github.qeroney.model.TelegramUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TelegramUserRepository : JpaRepository<TelegramUser, UUID> {
    fun findByChatId(chatId: Long): TelegramUser?
}