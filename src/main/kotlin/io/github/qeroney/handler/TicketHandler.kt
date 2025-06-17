package io.github.qeroney.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.container.GeneralContainer
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.DOCUMENT
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.PHOTO
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.TEXT
import io.github.dehuckakpyt.telegrambot.ext.container.fromId
import io.github.dehuckakpyt.telegrambot.factory.keyboard.inlineKeyboard
import io.github.dehuckakpyt.telegrambot.factory.keyboard.removeKeyboard
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import io.github.qeroney.config.properties.MessageTemplate
import io.github.qeroney.model.Attachment
import io.github.qeroney.model.FileType
import io.github.qeroney.service.ticket.TicketService
import io.github.qeroney.service.ticket.argument.CreateTicketArgument
import io.github.qeroney.service.user.TelegramUserService
// todo(перенести весь текст в template)
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
        sendMessage(
            "🎫 *Создание новой заявки*\n\n" +
                    "Пожалуйста, опишите, в чем проблема? (1–2 предложения)",
            parseMode = "Markdown",
            replyMarkup = removeKeyboard())
    }

    step("ask_description", type = TEXT) {
        val desc = text.trim()

        continueTransferringPlus("description" to desc)

        sendMessage(
            "✅ *Принял описание:* $desc\n\n" +
                    "Хотите прикрепить вложения? Можно до 5 штук.",
            parseMode = "Markdown",
            replyMarkup = inlineKeyboard(
                callbackButton("📎 Прикрепить файлы", "attach_files"),
                callbackButton("⏭️ Пропустить",       "skip_attachments")))
    }

    callback("attach_files", next = "receiving_files") {
        val map = transferred<MutableMap<String, Any>>()
        transfer(map)

        sendMessage("📎 *Прикрепление файлов*\n\n" +
                    "Отправьте фото или документы. После каждого — увидите счётчик и кнопку «Готово».\n" +
                    "Максимум 5.",
            parseMode = "Markdown",
            replyMarkup = removeKeyboard())
    }

    callback("skip_attachments", next = "confirm_ticket") {
        val map         = transferred<MutableMap<String, Any>>()
        val files       = map["files"] as List<Attachment>
        val user        = telegramUserService.getByChatId(fromId)
        val description = map["description"] as String

        transfer(map)

        val summary = buildString {
            append("📋 *Подтверждение заявки*\n\n")
            append("👤 ФИО: ${user.fullName ?: "Не указано"}\n")
            append("✉️ Почта: ${user.email     ?: "Не указана"}\n")
            append("📱 Контакт: ${user.phone    ?: "Не указан"}\n")
            append("📝 Описание: $description\n")
            append("📎 Вложения: ${files.size} шт.\n\n")
            append("Всё верно?")
        }

        sendMessage(summary,
            parseMode = "Markdown",
            replyMarkup = inlineKeyboard(callbackButton("✅ Отправить заявку", "submit_ticket"),
                                         callbackButton("❌ Отменить", "cancel_ticket")))
    }

    step("receiving_files", type = DOCUMENT) {
        val map   = transferred<MutableMap<String, Any>>()
        transfer(map)
        val files = map["files"] as MutableList<Attachment>

        if (files.size >= 5) {
            sendMessage("⚠️ Максимум 5 файлов! Нажмите «Готово» для продолжения.")
            return@step
        }

        files.add(Attachment(
                fileId   = document.fileId,
                fileName = document.fileName ?: "document",
                type     = FileType.DOCUMENT,
                fileSize = document.fileSize))
        transfer(map)

        val names = files.joinToString(", ") { it.fileName.toString() }
        sendMessage(
            "✅ Вложения: $names\n" +
                    "📊 Получено файлов: ${files.size}\n" +
                    "Отправь ещё или нажми «Готово»",
            parseMode = "Markdown",
            replyMarkup = inlineKeyboard(callbackButton("✅ Готово", "done_attachments")))
    }

    step("receiving_files", type = PHOTO) {
        val best  = photos.maxByOrNull { it.fileSize ?: 0 }!!
        val map   = transferred<MutableMap<String, Any>>()
        val files = map["files"] as MutableList<Attachment>

        if (files.size >= 5) {
            sendMessage("⚠️ Максимум 5 файлов! Нажмите «Готово» для продолжения.")
            return@step
        }

        files.add( Attachment(
            fileId   = best.fileId,
            fileName = "photo_${System.currentTimeMillis()}.jpg",
            type     = FileType.PHOTO,
            fileSize = best.fileSize?.toLong()))
        transfer(map)

        val names = files.joinToString(", ") { it.fileName.toString() }
        sendMessage("✅ Вложения: $names\n" +
                            "📊 Получено файлов: ${files.size}\n" +
                            "Отправь ещё или нажми «Готово»",
                    parseMode = "Markdown",
                    replyMarkup = inlineKeyboard(callbackButton("✅ Готово", "done_attachments")))
    }

    callback("done_attachments", next = "confirm_ticket") {
        val map         = transferred<MutableMap<String, Any>>()
        val files       = map["files"] as List<Attachment>
        val user        = telegramUserService.getByChatId(fromId)
        val description = map["description"] as String

        val summary = buildString {
            append("📋 *Подтверждение заявки*\n\n")
            append("👤 ФИО: ${user.fullName ?: "Не указано"}\n")
            append("✉️ Почта: ${user.email     ?: "Не указана"}\n")
            append("📱 Контакт: ${user.phone    ?: "Не указан"}\n")
            append("📝 Описание: $description\n")
            append("📎 Вложения: ${files.size} шт.\n\n")
            append("Всё верно?")
        }

        sendMessage(summary,
                    parseMode = "Markdown",
                    replyMarkup = inlineKeyboard(callbackButton("✅ Отправить заявку", "submit_ticket"),
                                                 callbackButton("❌ Отменить",         "cancel_ticket")))
    }

    callback("submit_ticket") {
        val map   = transferred<MutableMap<String, Any>>()
        val user  = telegramUserService.getByChatId(fromId)
        val desc  = map["description"] as String
        val files = map["files"]       as List<Attachment>

        val ticket = ticketService.upsert(CreateTicketArgument(
            description     = desc,
            files           = files,
            attachmentCount = files.size,
            owner           = user))
        sendMessage("✅ Заявка #${ticket.id} создана!", replyMarkup = removeKeyboard())
    }

    callback("cancel_ticket") {
        transfer(mutableMapOf<String, Any>())
        sendMessage("❌ Создание заявки отменено.", replyMarkup = removeKeyboard())
    }
})