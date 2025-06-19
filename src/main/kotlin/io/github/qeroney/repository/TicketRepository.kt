package io.github.qeroney.repository

import io.github.qeroney.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface TicketRepository: JpaRepository<Ticket, Long> {
    fun findAllByOwnerChatId(chatId: Long): List<Ticket>?

    @Query("SELECT t FROM Ticket t JOIN FETCH t.owner WHERE t.submittedAt BETWEEN :from AND :to")
    fun findAllBySubmittedAtBetween(from: LocalDateTime, to: LocalDateTime): List<Ticket>?
}