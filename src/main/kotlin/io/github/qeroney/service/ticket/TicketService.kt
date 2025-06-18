package io.github.qeroney.service.ticket

import io.github.dehuckakpyt.telegrambot.transaction.action.TransactionAction
import io.github.qeroney.exception.ConflictException
import io.github.qeroney.model.Ticket
import io.github.qeroney.repository.TicketRepository
import io.github.qeroney.service.ticket.argument.CreateTicketArgument

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TicketService(
    private val transactional: TransactionAction,
    private val repository: TicketRepository) {

    suspend fun create(arg: CreateTicketArgument): Ticket = transactional {
        repository.save(Ticket(
            submittedAt     = LocalDateTime.now(),
            attachmentCount = arg.attachmentCount,
            files           = arg.files,
            description     = arg.description,
            owner           = arg.owner))
    }

    suspend fun getTicketsByOwnerChatId(chatId: Long): List<Ticket> = transactional(readOnly = true) {
        repository.findAllByOwnerChatId(chatId)
            ?: throw ConflictException("Tickets.notFound")
    }
}