package io.github.qeroney.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.container.GeneralContainer
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.DOCUMENT
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.PHOTO
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.TEXT
import io.github.dehuckakpyt.telegrambot.ext.container.fromId
import io.github.dehuckakpyt.telegrambot.factory.keyboard.inlineKeyboard
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import io.github.qeroney.config.properties.MessageTemplate
import io.github.qeroney.model.Attachment
import io.github.qeroney.model.FileType
import io.github.qeroney.service.ticket.TicketService
import io.github.qeroney.service.ticket.argument.CreateTicketArgument
import io.github.qeroney.service.user.TelegramUserService

@HandlerComponent
class TicketHandler(
    private val ticketService: TicketService,
    private val telegramUserService: TelegramUserService,
    private val template: MessageTemplate) : BotHandler({

    fun GeneralContainer.continueTransferringPlus(vararg pairs: Pair<String, Any>): MutableMap<String, Any> {
        val map = transferredOrNull<MutableMap<String, Any>>() ?: mutableMapOf()
        pairs.forEach { (k, v) -> map[k] = v }
        transfer(map)
        return map
    }

    callback("create_ticket", next = "ask_description") {
        transfer(mutableMapOf("files" to mutableListOf<Attachment>()))
        sendMessage(template.getTicketAskDescription)
    }

    step("ask_description", type = TEXT, next = "ask_attachments") {
        val desc = text.trim()

        val draft = continueTransferringPlus("description" to desc)

        sendMessage(template.getTicketDescriptionAccepted with mapOf("desc" to desc),
            replyMarkup = inlineKeyboard(
                callbackButton("📎 Прикрепить файлы", "attach_files", draft),
                callbackButton("⏭️ Пропустить", "skip_attachments", draft)))
    }

    callback("attach_files", next = "receiving_files") {
        transfer(transferred())
        sendMessage(template.getTicketAttachFilesPrompt)
    }

    callback("skip_attachments", next = "confirm_ticket") {
        val map = transferred<MutableMap<String, Any>>()
        val files = map["files"] as MutableList<Attachment>
        val user = telegramUserService.getByChatId(fromId)
        val description = map["description"] as? String ?: "не указано"
        transfer(map)

        val confirmationText = buildString {
            append("📋 *Подтверждение заявки*\n\n")
            append("Проверьте данные перед отправкой заявки:\n\n")
            append("👤 *ФИО:* ${user.fullName ?: "Не указано"}\n")
            append("📧 *Почта:* ${user.email ?: "Не указано"}\n")
            append("📱 *Контакт:* ${user.phone ?: "Не указано"}\n")
            append("📝 *Описание:* $description\n")
            append("📎 *Вложения:* ${files.size} файла(ов)\n\n")
            append("Всё верно?")
        }

        sendMessage(confirmationText,
            replyMarkup = inlineKeyboard(
                callbackButton(template.getSendTicket, "submit_ticket", map),
                callbackButton(template.getCancelTicket, "cancel_ticket", map)))
    }

    step("receiving_files", type = DOCUMENT, next = "receiving_files") {
        val doc = document
        val map = transferred<MutableMap<String, Any>>()
        val files = map["files"] as MutableList<Attachment>

        if (files.size >= 5) {
            sendMessage(template.getTicketMaxFilesError)
            return@step
        }

        files.add(Attachment(
            fileId = doc.fileId,
            fileName = doc.fileName ?: "document",
            type = FileType.DOCUMENT,
            fileSize = doc.fileSize))

        map["files"] = files
        transfer(map)

        sendMessage(template.getTicketFileSaved with mapOf("fileName" to doc.fileName, "size" to files.size),
            replyMarkup = inlineKeyboard(callbackButton(template.getDoneTicket, "done_attachments", map)))
    }

    step("receiving_files", type = PHOTO, next = "receiving_files") {
        val photo = photos.maxByOrNull { it.fileSize ?: 0 } ?: photos.last()
        val map = transferred<MutableMap<String, Any>>()
        val files = map["files"] as MutableList<Attachment>

        if (files.size >= 5) {
            sendMessage(template.getTicketMaxFilesError)
            return@step
        }

        files.add(Attachment(
            fileId = photo.fileId,
            fileName = "photo_${System.currentTimeMillis()}.jpg",
            type = FileType.PHOTO,
            fileSize = photo.fileSize?.toLong()))

        map["files"] = files
        transfer(map)

        sendMessage(template.getTicketPhotoSaved with mapOf("size" to files.size),
            replyMarkup = inlineKeyboard(callbackButton(template.getDoneTicket, "done_attachments", map)))
    }

    step("receiving_files", type = TEXT, next = "receiving_files") {
        sendMessage(template.getTicketNoTextAllowed)
    }

    callback("done_attachments", next = "confirm_ticket") {
        val map = transferred<MutableMap<String, Any>>()
        val files = map["files"] as MutableList<Attachment>
        val user = telegramUserService.getByChatId(fromId)
        val description = map["description"] as? String ?: "не указано"
        transfer(map)

        val confirmationText = buildString {
            append("📋 *Подтверждение заявки*\n\n")
            append("Проверьте данные перед отправкой заявки:\n\n")
            append("👤 *ФИО:* ${user.fullName ?: "Не указано"}\n")
            append("📧 *Почта:* ${user.email ?: "Не указано"}\n")
            append("📱 *Контакт:* ${user.phone ?: "Не указано"}\n")
            append("📝 *Описание:* $description\n")
            append("📎 *Вложения:* ${files.size} файла(ов)\n\n")
            append("Всё верно?")
        }

        sendMessage(confirmationText,
            replyMarkup = inlineKeyboard(
                callbackButton(template.getSendTicket, "submit_ticket", map),
                callbackButton(template.getCancelTicket, "cancel_ticket", map)))
    }

    callback("submit_ticket") {
        val map = transferred<MutableMap<String, Any>>()
        val user = telegramUserService.getByChatId(fromId)
        transfer(map)

        sendMessage(template.getFinalSendTicket)

        val description = map["description"] as? String
        val files = map["files"] as MutableList<Attachment>

        val arg = CreateTicketArgument(
            description = description,
            files = files,
            attachmentCount = files.size,
            owner = user)

        val ticket = ticketService.create(arg)

        sendMessage(
            "🎉 *Заявка успешно создана!*\n\n" +
                    "📋 *Номер вашей заявки:* #${ticket.id}\n" +
                    "📝 *Описание:* ${ticket.description}\n" +
                    "📎 *Вложений:* ${files.size}\n\n" +
                    "Ваша заявка принята в обработку. Мы свяжемся с вами в ближайшее время.",
            parseMode = "Markdown",
            replyMarkup = inlineKeyboard(
                callbackButton(template.getAnotherOneTicket, "create_ticket"),
                callbackButton(template.getMyTicketsButton, "my_tickets")))
    }

    callback("cancel_ticket") {
        sendMessage(template.getCancelTicketAndMainMenu,
                    replyMarkup = inlineKeyboard(callbackButton(template.getMainMenu, "main_menu"),
                                                 callbackButton(template.getNewTicket, "create_ticket")))
    }

    callback("my_tickets") {
        val user = telegramUserService.getByChatId(fromId)
        val tickets = ticketService.getTicketsByOwnerChatId(user.chatId)

        if (tickets.isEmpty()) {
            sendMessage(
                "📋 *Мои заявки*\n\n" +
                        "У вас ещё нет заявок.",
                parseMode = "Markdown",
                replyMarkup = inlineKeyboard(callbackButton(template.getAnotherNewTicket, "create_ticket")))
        } else {
            val text = buildString {
                append("📋 *Ваши заявки:*\n\n")
                tickets.forEachIndexed { index, ticket ->
                    append("${index + 1}. *#${ticket.id}* — ${ticket.description ?: "без описания"}\n")
                    ticket.attachmentCount?.let {
                        if (it > 0) {
                            append("📎 Вложений: ${ticket.attachmentCount}\n")
                        }
                    }
                    append("\n")
                }
            }
            sendMessage(text, parseMode = "Markdown", replyMarkup = inlineKeyboard(callbackButton(template.getAnotherNewTicket, "create_ticket")))
            }
        }

        callback("main_menu") {
            sendMessage(template.getMainMenuPrompt,
                replyMarkup = inlineKeyboard(
                    callbackButton(template.getAnotherNewTicket, "create_ticket"),
                    callbackButton(template.getMyTicketsButton, "my_tickets")))
        }
    })