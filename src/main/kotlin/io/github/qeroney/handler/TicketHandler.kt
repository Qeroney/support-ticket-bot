package io.github.qeroney.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.container.GeneralContainer
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
import io.github.qeroney.service.notification.argument.CreateTicketNotificationArgument
import io.github.qeroney.service.ticket.TicketService
import io.github.qeroney.service.ticket.argument.CreateTicketArgument
import io.github.qeroney.service.user.TelegramUserService
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

@HandlerComponent
class TicketHandler(
    private val ticketService: TicketService,
    private val telegramUserService: TelegramUserService,
    private val template: MessageTemplate,
    private val notificationService: SendNotificationService) : BotHandler({

    val filesStorage = ConcurrentHashMap<Long, MutableList<Attachment>>()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'МСК'")

    fun GeneralContainer.continueTransferringPlus(vararg pairs: Pair<String, Any>): MutableMap<String, Any> {
        val map = transferredOrNull<MutableMap<String, Any>>() ?: mutableMapOf()
        pairs.forEach { (k, v) -> map[k] = v }
        transfer(map)
        return map
    }

    fun Any?.asAttachment(): Attachment? = runCatching {
        when (this) {
            is Attachment -> this
            is Map<*, *> -> Attachment(
                fileId = this["fileId"] as String,
                fileName = this["fileName"] as? String,
                type = FileType.valueOf(this["type"] as String),
                fileSize = (this["fileSize"] as? Number)?.toLong())
            else -> null
        } }.getOrNull()


    callback("create_ticket", next = "ask_description") {
        filesStorage[fromId] = mutableListOf()
        transfer(mutableMapOf("files" to mutableListOf<Attachment>()))
        sendMessage(template.ticketAskDescription)
    }

    step("ask_description", type = TEXT, next = "ask_attachments") {
        val desc = text.trim()
        val draft = continueTransferringPlus("description" to text.trim())

        sendMessage(template.ticketDescriptionAccepted with mapOf("desc" to desc),
                    replyMarkup = inlineKeyboard(callbackButton(template.ticketAttachFiles, "attach_files", draft),
                        callbackButton(template.ticketSkipAttachFiles, "skip_attachments", draft)))
    }

    callback("attach_files", next = "receiving_files") {
        filesStorage.putIfAbsent(fromId, mutableListOf())
        val map = transferred<MutableMap<String, Any>>()
        transfer(map)
        sendMessage(template.ticketAttachFilesPrompt,
                    replyMarkup = inlineKeyboard(callbackButton(template.doneTicket, "done_attachments", map)))
    }

    callback("skip_attachments", next = "confirm_ticket") {
        val map = transferred<MutableMap<String, Any>>()
        val files = filesStorage[fromId] ?: mutableListOf()
        val user = telegramUserService.getByChatId(fromId)
        val description = map["description"] as? String
        map["files"] = files

        sendMessage(template.ticketConfirmation with mapOf("fullName" to user.fullName, "email" to user.email, "phone" to user.phone, "description" to description, "filesCount" to files.size),
            replyMarkup = inlineKeyboard(
                callbackButton(template.sendTicket, "submit_ticket", map),
                callbackButton(template.cancelTicket, "cancel_ticket", map)))
    }

    step("receiving_files", type = DOCUMENT, next = "receiving_files") {
        val doc = document
        val files = filesStorage.getOrPut(fromId) { mutableListOf() }

        if (files.size >= 5) {
            sendMessage(template.ticketMaxFilesError)
            return@step
        }

        files.add(Attachment(
            fileId = doc.fileId,
            fileName = doc.fileName ?: "document",
            type = FileType.DOCUMENT,
            fileSize = doc.fileSize))

        bot.save(doc.fileId, doc.fileName)

        sendMessage(template.ticketDocumentAdded with mapOf("fileName" to doc.fileName))
    }

    step("receiving_files", type = PHOTO, next = "receiving_files") {
        val photos = photos.maxByOrNull { it.fileSize ?: 0 } ?: photos.last()
        val files = filesStorage.getOrPut(fromId) { mutableListOf() }

        if (files.size >= 5) {
            sendMessage(template.ticketMaxFilesError)
            return@step
        }

        val fileName = "photo_${photos.fileId}.jpg"
        files.add(Attachment(
            fileId = photos.fileId,
            fileName = fileName,
            type = FileType.PHOTO,
            fileSize = photos.fileSize?.toLong()))

        bot.save(photos.fileId, fileName)

        sendMessage(template.ticketPhotoAdded with mapOf("fileName" to fileName))
    }

     step("receiving_files", type = TEXT, next = "receiving_files") {
         throw ChatException(template.ticketNoTextAllowed)
     }

     callback("done_attachments", next = "confirm_ticket") {
         val map = transferred<MutableMap<String, Any>>()
         val files = filesStorage[fromId] ?: mutableListOf()
         val user = telegramUserService.getByChatId(fromId)
         val description = map["description"] as? String
         map["files"] = files
         sendMessage(template.ticketConfirmation with mapOf(
             "fullName" to (user.fullName ?: "Не указано"),
             "email" to (user.email ?: "Не указано"),
             "phone" to (user.phone ?: "Не указано"),
             "description" to description,
             "filesCount" to files.size),

             replyMarkup = inlineKeyboard(
                 callbackButton(template.sendTicket, "submit_ticket", map),
                 callbackButton(template.cancelTicket, "cancel_ticket", map)))
     }

     callback("submit_ticket") {
         val map = transferred<MutableMap<String, Any>>()
         val user = telegramUserService.getByChatId(fromId)
         val description = map["description"] as? String
         val filesListRaw = map["files"] as? List<*> ?: filesStorage[fromId] ?: mutableListOf()
         val files = filesListRaw.mapNotNull { it.asAttachment() }.toMutableList()

         sendMessage(template.finalSendTicket)

         val arg = CreateTicketArgument(
             description = description,
             files = files,
             attachmentCount = files.size,
             owner = user)

         val ticket = ticketService.create(arg)

         notificationService.sendTicketNotification(CreateTicketNotificationArgument(
             to = "vadimvetrov2015@mail.ru",
             fio = user.fullName ?: "Не указано",
             email = user.email ?: "Не указано",
             phone = user.phone ?: "Не указано",
             description = ticket.description ?: "Без описания",
             attachments = files,
             createdAt = "[${ticket.submittedAt?.format(formatter)}]"))

         filesStorage.remove(fromId)

          sendMessage(template.ticketCreatedMessage with mapOf("ticketId" to ticket.id),
              replyMarkup = inlineKeyboard(callbackButton(template.anotherOneTicket, "create_ticket"),
                                           callbackButton(template.myTicketsButton, "my_tickets")))
     }

     callback("cancel_ticket") {
         filesStorage.remove(fromId)
         sendMessage(template.cancelTicketAndMainMenu,
                     replyMarkup = inlineKeyboard(callbackButton(template.mainMenu, "main_menu"),
                                                  callbackButton(template.newTicket, "create_ticket")))
     }

     callback("my_tickets") {
         val user = telegramUserService.getByChatId(fromId)
         val tickets = ticketService.getTicketsByOwnerChatId(user.chatId)

         val ticketsText = tickets.mapIndexed { index, ticket ->
             val attachmentsText = ticket.attachmentCount?.takeIf { it > 0 }?.let {
                 template.ticketAttachments with mapOf("attachmentsCount" to it)
             } ?: ""
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