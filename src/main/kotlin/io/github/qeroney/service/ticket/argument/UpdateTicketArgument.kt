package io.github.qeroney.service.ticket.argument

import io.github.qeroney.model.Attachment

data class UpdateTicketArgument(
    val id: Long,
    val files: List<Attachment>)