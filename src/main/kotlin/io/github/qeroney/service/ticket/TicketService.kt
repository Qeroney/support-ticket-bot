package io.github.qeroney.service.ticket

import com.antkorwin.xsync.XSync
import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.transaction.action.TransactionAction
import io.github.qeroney.model.Ticket
import io.github.qeroney.repository.TicketRepository
import io.github.qeroney.service.ticket.argument.CreateTicketArgument
import io.github.qeroney.service.ticket.argument.UpdateTicketArgument

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TicketService(
    private val transactional: TransactionAction,
    private val repository: TicketRepository,
    private val chatIdSync: XSync<Long>) {

    suspend fun create(arg: CreateTicketArgument): Ticket = transactional {
        repository.save(Ticket(
            submittedAt     = LocalDateTime.now(),
            attachmentCount = arg.attachmentCount,
            files           = arg.files,
            description     = arg.description,
            owner           = arg.owner))
    }

    suspend fun update(arg: UpdateTicketArgument): Ticket = chatIdSync.evaluate(arg.id) {
        val ticket = repository.findById(arg.id).orElseThrow { ChatException("Заявка не найдена") }

        ticket.files = (ticket.files ?: mutableListOf()).toMutableList().apply { addAll(arg.files) }
        ticket.attachmentCount = ticket.files?.size

        repository.save(ticket)
    }

    suspend fun getLastByOwnerId(chatId: Long): Ticket = transactional(readOnly = true) {
        repository.findFirstByOwnerChatIdOrderByIdDesc(chatId) ?: throw ChatException("Заявка не найдена")
    }

    suspend fun getAllByOwnerChatId(chatId: Long): List<Ticket> = transactional(readOnly = true) {
        repository.findAllByOwnerChatId(chatId)
            ?: throw ChatException("Заявки не найдены")
    }

    suspend fun getAllBySubmittedAtBetween(from: LocalDateTime, to: LocalDateTime): List<Ticket> = transactional(readOnly = true) {
        repository.findAllBySubmittedAtBetween(from, to)
            ?: throw ChatException("Заявки не найдены")
    }

    suspend fun deleteById(id: Long) = transactional {
        if (repository.existsById(id)) {
            repository.deleteById(id)
        }
    }
}
