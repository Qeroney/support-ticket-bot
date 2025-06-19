package io.github.qeroney.service.notification.argument

import io.github.qeroney.model.Attachment

data class CreateTicketNotificationArgument(
    val to: String,
    val fio: String,
    val email: String,
    val phone: String,
    val description: String,
    val attachments: List<Attachment> = emptyList(),
    val createdAt: String)