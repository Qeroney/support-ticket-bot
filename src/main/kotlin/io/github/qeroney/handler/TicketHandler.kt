package io.github.qeroney.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.DOCUMENT
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.PHOTO
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.TEXT
import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.ext.container.fromId
import io.github.dehuckakpyt.telegrambot.factory.keyboard.inlineKeyboard
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import io.github.qeroney.config.properties.MessageTemplate
import io.github.qeroney.ext.save
import io.github.qeroney.model.Attachment
import io.github.qeroney.model.FileType
import io.github.qeroney.service.notification.SendNotificationService
import io.github.qeroney.service.ticket.TicketService
import io.github.qeroney.service.ticket.argument.CreateTicketArgument
import io.github.qeroney.service.ticket.argument.UpdateTicketArgument
import io.github.qeroney.service.user.TelegramUserService

@HandlerComponent
class TicketHandler(
    private val ticketService: TicketService,
    private val telegramUserService: TelegramUserService,
    private val template: MessageTemplate,
    private val notificationService: SendNotificationService) : BotHandler({

    callback("create_ticket", next = "ask_description") {
        sendMessage(template.ticketAskDescription)
    }

    step("ask_description", type = TEXT, next = "ask_attachments") {
        val desc = text.trim()
        val user = telegramUserService.getByChatId(fromId)
        ticketService.create(CreateTicketArgument(description = desc, files = listOf(), attachmentCount = 0, owner = user))

        sendMessage(template.ticketDescriptionAccepted with mapOf("desc" to desc),
            replyMarkup = inlineKeyboard(
                callbackButton(template.ticketAttachFiles, "attach_files"),
                callbackButton(template.ticketSkipAttachFiles, "skip_attachments")))
    }

    callback("attach_files", next = "receiving_files") {
        sendMessage(template.ticketAttachFilesPrompt,
                    replyMarkup = inlineKeyboard(callbackButton(template.doneTicket, "done_attachments")))
    }

    callback("skip_attachments", next = "confirm_ticket") {
        val ticket = ticketService.getLastByOwnerId(fromId)
        sendMessage(template.ticketConfirmation with mapOf("fullName" to ticket.owner.fullName, "email" to ticket.owner.email, "phone" to ticket.owner.phone, "description" to ticket.description, "filesCount" to ticket.files!!.size),
                    replyMarkup = inlineKeyboard(callbackButton(template.sendTicket, "submit_ticket"),
                        callbackButton(template.cancelTicket, "cancel_ticket")))
    }

    step("receiving_files", type = DOCUMENT, next = "receiving_files") {
        val doc = document
        val ticket = ticketService.getLastByOwnerId(fromId)
        val attachment = Attachment(fileId = doc.fileId, fileName = doc.fileName, type = FileType.DOCUMENT, fileSize = doc.fileSize)
        ticketService.update(UpdateTicketArgument(ticket.id!!, listOf(attachment)))
        bot.save(doc.fileId)

        sendMessage(template.ticketDocumentAdded with mapOf("fileName" to doc.fileName))
    }

    step("receiving_files", type = PHOTO, next = "receiving_files") {
        val photo = photos.maxByOrNull { it.fileSize ?: 0 } ?: photos.last()
        val ticket = ticketService.getLastByOwnerId(fromId)

        if (ticket.files?.size!! >= 5) {
            sendMessage(template.ticketMaxFilesError)
            return@step
        }
        val fileName = "photo_${photo.fileId}.jpg"
        val attachment = Attachment(fileId = photo.fileId, fileName = fileName, type = FileType.PHOTO, fileSize = photo.fileSize?.toLong())
        ticketService.update(UpdateTicketArgument(ticket.id!!, listOf(attachment)))
        bot.save(photo.fileId)

        sendMessage(template.ticketPhotoAdded with mapOf("fileName" to fileName))
    }

    step("receiving_files", type = TEXT, next = "receiving_files") {
        throw ChatException(template.ticketNoTextAllowed)
    }

    callback("done_attachments", next = "confirm_ticket") {
        val ticket = ticketService.getLastByOwnerId(fromId)
        sendMessage(template.ticketConfirmation with mapOf(
                "fullName" to ticket.owner.fullName,
                "email" to ticket.owner.email,
                "phone" to ticket.owner.phone,
                "description" to ticket.description,
                "filesCount" to ticket.files!!.size),

            replyMarkup = inlineKeyboard(callbackButton(template.sendTicket, "submit_ticket"),
                callbackButton(template.cancelTicket, "cancel_ticket")))
    }

    callback("submit_ticket") {
        val ticket = ticketService.getLastByOwnerId(fromId)
        sendMessage(template.finalSendTicket)
        notificationService.sendTicketNotification(ticket)

        sendMessage(template.ticketCreatedMessage with mapOf("ticketId" to ticket.id),
            replyMarkup = inlineKeyboard(
                callbackButton(template.anotherOneTicket, "create_ticket"),
                callbackButton(template.myTicketsButton, "my_tickets")))
    }

    callback("cancel_ticket") {
        val ticket = ticketService.getLastByOwnerId(fromId)
        ticketService.deleteById(ticket.id!!)
        sendMessage(template.cancelTicketAndMainMenu,
                replyMarkup = inlineKeyboard(
                    callbackButton(template.mainMenu, "main_menu"),
                    callbackButton(template.newTicket, "create_ticket")))
    }

    callback("my_tickets") {
        val user = telegramUserService.getByChatId(fromId)
        val tickets = ticketService.getAllByOwnerChatId(user.chatId)

        val ticketsText = tickets.mapIndexed { index, ticket ->
            val attachmentsText = ticket.attachmentCount?.takeIf { it > 0 }?.let {
                template.ticketAttachments with mapOf("attachmentsCount" to it) } ?: ""
            template.ticketLine with mapOf(
                "index" to (index + 1),
                "ticketId" to ticket.id,
                "description" to ticket.description,
                "attachments" to attachmentsText)
        }.joinToString("\n")

        sendMessage(if (tickets.isEmpty()) template.ticketsEmpty else template.ticketsHeader + "\n\n" + ticketsText,
            replyMarkup = inlineKeyboard(callbackButton(template.anotherNewTicket, "create_ticket")))
    }

    callback("main_menu") {
        sendMessage(template.mainMenuPrompt,
                    replyMarkup = inlineKeyboard(
                        callbackButton(template.anotherNewTicket, "create_ticket"),
                        callbackButton(template.myTicketsButton, "my_tickets")))
    }
})