package io.github.qeroney.repository

import io.github.qeroney.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface TicketRepository: JpaRepository<Ticket, Long> {
    fun findFirstByOwnerChatIdOrderByIdDesc(chatId: Long): Ticket?
    fun findAllByOwnerChatId(chatId: Long): List<Ticket>?
    fun findAllBySubmittedAtBetween(from: LocalDateTime, to: LocalDateTime): List<Ticket>?
}