package io.github.qeroney.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.PropertySource

@PropertySource("classpath:messages.properties")
@ConfigurationProperties(prefix = "telegram-bot.template")
class MessageTemplate @ConstructorBinding constructor(

    val getStartWelcome: String,
    val getRegistration: String,
    val getAskFullName: String,
    val getFullNameFormatError: String,
    val getFullNameSaved: String,
    val getEmailPrompt: String,
    val getEmailFormatError: String,
    val getEmailSaved: String,
    val getAskContact: String,
    val getSendContact: String,
    val getContactOnlyByButton: String,
    val getContactWrongUser: String,
    val getContactSaved: String,
    val getRegistrationSuccess: String,
    val getCreateTicketButton: String,
    val getMyTicketsButton: String,
    val getEmailSent: String,
    val getEmailCodeMismatchError: String,
    val getEmailChange: String,
    val getNewEmail: String,

    )