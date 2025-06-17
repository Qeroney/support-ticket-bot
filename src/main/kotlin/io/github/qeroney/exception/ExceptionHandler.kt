package io.github.qeroney.exception

import io.github.dehuckakpyt.telegrambot.TelegramBot
import io.github.dehuckakpyt.telegrambot.exception.handler.ExceptionHandlerImpl
import io.github.dehuckakpyt.telegrambot.model.telegram.Chat
import io.github.dehuckakpyt.telegrambot.template.MessageTemplate
import io.github.dehuckakpyt.telegrambot.template.Templater

class ExceptionHandler(
    bot: TelegramBot,
    template: MessageTemplate,
    templater: Templater) : ExceptionHandlerImpl(bot, template, templater) {

    override suspend fun caught(chat: Chat, ex: Throwable) {
        when (ex) {
            is ConflictException -> bot.sendMessage(
                chat.id,
                template.whenKnownException with ("message" to ex.localizedMessage))
            else-> super.caught(chat, ex)
        }
    }
}