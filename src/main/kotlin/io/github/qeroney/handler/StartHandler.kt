package io.github.qeroney.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.container.GeneralContainer
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.CONTACT
import io.github.dehuckakpyt.telegrambot.container.message.MessageType.TEXT
import io.github.dehuckakpyt.telegrambot.exception.chat.ChatException
import io.github.dehuckakpyt.telegrambot.ext.container.fromId
import io.github.dehuckakpyt.telegrambot.factory.keyboard.contactKeyboard
import io.github.dehuckakpyt.telegrambot.factory.keyboard.inlineKeyboard
import io.github.dehuckakpyt.telegrambot.factory.keyboard.removeKeyboard
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import io.github.qeroney.config.properties.MessageTemplate
import io.github.qeroney.service.notification.SendNotificationService
import io.github.qeroney.service.user.TelegramUserService
import io.github.qeroney.service.user.argument.CreateTelegramUser
import kotlin.random.Random

@HandlerComponent
class StartHandler(
    private val telegramUserService: TelegramUserService,
    private val notificationService: SendNotificationService,
    private val template: MessageTemplate) : BotHandler({

    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")

    fun GeneralContainer.continueTransferringPlus(vararg keyValues: Pair<String, Any>): MutableMap<String, Any> {
        val map = transferredOrNull<MutableMap<String, Any>>() ?: mutableMapOf()
        keyValues.forEach { (key, value) -> map[key] = value }
        transfer(map)
        return map
    }

    command("/start") {
        sendMessage(template.startWelcome,
            replyMarkup = inlineKeyboard(callbackButton(template.registration, "get_fullName")))
    }

    callback("get_fullName", next = "get_enter_fullName") {
        sendMessage(template.askFullName)
    }

    step("get_enter_fullName", type = TEXT, next = "get_email") {
        val input = text.trim()
        if (input.length < 5 || !input.contains(' ')) throw ChatException(template.fullNameFormatError)

        continueTransferringPlus("fullName" to input)

        sendMessage(template.fullNameSaved with mapOf("fullName" to input), replyMarkup = removeKeyboard())
        sendMessage(template.emailPrompt, replyMarkup = removeKeyboard())
    }

    step("get_email", type = TEXT, next = "get_confirm_email") {
        val email = text.trim().lowercase()
        if (!emailRegex.matches(email)) throw ChatException(template.emailFormatError)

        val verificationCode = Random.nextInt(0, 1_000_000).let { String.format("%06d", it) }
        continueTransferringPlus("email" to email, "verificationCode" to verificationCode)

        notificationService.sendVerificationCodeToEmail(email, verificationCode)
        sendMessage(template.emailSent with mapOf("email" to email),
                    replyMarkup = inlineKeyboard(callbackButton(template.emailChange, "get_change_email")))
    }

    callback("get_change_email", next = "get_email") {
        transfer(transferred())
        sendMessage(template.newEmail, replyMarkup = removeKeyboard())
    }

    step("get_confirm_email", type = TEXT, next = "get_contact") {
        val inputCode = text.trim().lowercase()
        val map = transferred<MutableMap<String, Any>>()
        val realCode = map["verificationCode"]?.toString()
        transfer(map)

        if (inputCode != realCode) throw ChatException(template.emailCodeMismatchError)

        sendMessage(template.emailSaved with mapOf("email" to map["email"].toString()), replyMarkup = removeKeyboard())
        sendMessage(template.askContact, replyMarkup = contactKeyboard(template.sendContact))
    }

    step("get_contact", type = TEXT) {
        throw ChatException(template.contactOnlyByButton)
    }

    step("get_contact", type = CONTACT) {
        if (contact.userId != fromId) throw ChatException(template.contactWrongUser)

        val phone = contact.phoneNumber

        val map = transferred<MutableMap<String, Any>>()
        val createUser = CreateTelegramUser(
            chatId   = fromId,
            fullName = map["fullName"]!!.toString(),
            email    = map["email"]!!.toString(),
            phone    = phone)
        telegramUserService.upsert(createUser)

        sendMessage(template.contactSaved with mapOf("phone" to phone), replyMarkup = removeKeyboard())
        sendMessage(template.registrationSuccess,
                    replyMarkup = inlineKeyboard(
                        callbackButton(template.createTicketButton, "create_ticket"),
                        callbackButton(template.myTicketsButton, "my_tickets")))
    }
})