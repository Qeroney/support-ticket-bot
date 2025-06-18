package io.github.qeroney.repository

import io.github.qeroney.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepository: JpaRepository<Ticket, Long> {
    fun findAllByOwnerChatId(chatId: Long): List<Ticket>?
}