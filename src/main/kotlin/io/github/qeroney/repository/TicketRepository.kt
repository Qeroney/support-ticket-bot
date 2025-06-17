package io.github.qeroney.repository

import io.github.qeroney.model.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TicketRepository: JpaRepository<Ticket, UUID> {
    fun findByOwnerId(ownerId: UUID): Ticket?
    fun findAllByOwnerId(ownerId: UUID): List<Ticket>?
}