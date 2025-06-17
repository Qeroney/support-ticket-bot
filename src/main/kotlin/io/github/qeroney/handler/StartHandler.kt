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
import java.util.UUID

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

    command("/start", next = "get_fullName") {
        transfer(mutableMapOf<String, Any>())
        sendMessage(template.getStartWelcome,
            replyMarkup = inlineKeyboard(callbackButton(template.getRegistration, "get_fullName")))
    }

    callback("get_fullName", next = "get_enter_fullName") {
        sendMessage(template.getAskFullName)
    }

    step("get_enter_fullName", type = TEXT, next = "get_email") {
        val input = text.trim()
        if (input.length < 5 || !input.contains(' ')) throw ChatException(template.getFullNameFormatError)

        continueTransferringPlus("fullName" to input)

        sendMessage(template.getFullNameSaved.replace("{fullName}", input), replyMarkup = removeKeyboard())
        sendMessage(template.getEmailPrompt, replyMarkup = removeKeyboard())
    }

    step("get_email", type = TEXT, next = "get_confirm_email") {
        val email = text.trim().lowercase()
        if (!emailRegex.matches(email)) throw ChatException(template.getEmailFormatError)

        val verificationCode = UUID.randomUUID().toString().take(6).lowercase()
        continueTransferringPlus("email" to email, "verificationCode" to verificationCode)

        notificationService.sendVerificationCodeToEmail(email, verificationCode)
        sendMessage(template.getEmailSent.replace("{email}", email),
                    replyMarkup = inlineKeyboard(callbackButton(template.getEmailChange, "get_change_email")))
    }

    callback("get_change_email", next = "get_email") {
        transferred<MutableMap<String, Any>>()
        sendMessage(template.getNewEmail, replyMarkup = removeKeyboard())
    }

    step("get_confirm_email", type = TEXT, next = "get_contact") {
        val inputCode = text.trim().lowercase()
        val map = transferred<MutableMap<String, Any>>()
        val realCode = map["verificationCode"]?.toString()
        transfer(map)

        if (inputCode != realCode) throw ChatException(template.getEmailCodeMismatchError)

        sendMessage(template.getEmailSaved.replace("{email}", map["email"].toString()), replyMarkup = removeKeyboard())
        sendMessage(template.getAskContact, replyMarkup = contactKeyboard(template.getSendContact))
    }

    step("get_contact", type = TEXT) {
        throw ChatException(template.getContactOnlyByButton)
    }

    step("get_contact", type = CONTACT) {
        if (contact.userId != fromId) throw ChatException(template.getContactWrongUser)

        val phone = contact.phoneNumber

        val map = transferred<MutableMap<String, Any>>()
        val createUser = CreateTelegramUser(
            chatId   = fromId,
            fullName = map["fullName"]!!.toString(),
            email    = map["email"]!!.toString(),
            phone    = phone)
        telegramUserService.upsert(createUser)

        sendMessage(template.getContactSaved.replace("{phone}", phone), replyMarkup = removeKeyboard())
        sendMessage(template.getRegistrationSuccess,
                    replyMarkup = inlineKeyboard(
                        callbackButton(template.getCreateTicketButton, "create_ticket"),
                        callbackButton(template.getMyTicketsButton, "my_tickets")))
    }
})