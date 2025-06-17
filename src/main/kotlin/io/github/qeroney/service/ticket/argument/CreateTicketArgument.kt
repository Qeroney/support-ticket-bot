package io.github.qeroney.service.ticket.argument

import io.github.qeroney.model.Attachment
import io.github.qeroney.model.TelegramUser

data class CreateTicketArgument (
    val description: String? = null,
    val files: List<Attachment>,
    val attachmentCount: Int? = 0,
    val owner: TelegramUser,
)