package io.github.qeroney.service.ticket.argument

import io.github.qeroney.model.Attachment
import io.github.qeroney.model.TelegramUser
import io.github.qeroney.model.Ticket

data class CreateTicketArgument (
    val description: String?,
    val files: List<Attachment>,
    val attachmentCount: Int,
    val owner: TelegramUser
)