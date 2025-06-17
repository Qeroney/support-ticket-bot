package io.github.qeroney.service.ticket

import io.github.dehuckakpyt.telegrambot.transaction.action.TransactionAction
import io.github.qeroney.exception.ConflictException
import io.github.qeroney.model.Ticket
import io.github.qeroney.repository.TicketRepository
import io.github.qeroney.service.ticket.argument.CreateTicketArgument

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class TicketService(
    private val transactional: TransactionAction,
    private val repository: TicketRepository) {

    suspend fun upsert(arg: CreateTicketArgument): Ticket = transactional {
        val existingTicket = repository.findByOwnerId(arg.owner.id)

        existingTicket?.let { ticket ->
            ticket.apply {
                description = arg.description
                files = arg.files
                attachmentCount = arg.attachmentCount
            }
            repository.save(ticket)
        } ?: run {
            val ticket = Ticket(
                description = arg.description,
                files = arg.files,
                owner = arg.owner,
                submittedAt = LocalDateTime.now())
            repository.save(ticket)
        }
    }

    suspend fun getAllByOwnerId(ownerId: UUID) = transactional(readOnly = true) {
        repository.findAllByOwnerId(ownerId) ?: throw ConflictException("Tickets.notFound")
    }
}