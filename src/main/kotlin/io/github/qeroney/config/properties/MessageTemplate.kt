package io.github.qeroney.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "telegram-bot.template")
class MessageTemplate @ConstructorBinding constructor(

    val startWelcome: String,
    val registration: String,
    val askFullName: String,
    val fullNameFormatError: String,
    val fullNameSaved: String,
    val emailPrompt: String,
    val emailFormatError: String,
    val emailSaved: String,
    val askContact: String,
    val sendContact: String,
    val contactOnlyByButton: String,
    val contactWrongUser: String,
    val contactSaved: String,
    val registrationSuccess: String,
    val createTicketButton: String,
    val myTicketsButton: String,
    val emailSent: String,
    val emailCodeMismatchError: String,
    val emailChange: String,
    val newEmail: String,

    val mainMenuPrompt: String,
    val anotherNewTicket: String,
    val newTicket: String,
    val mainMenu: String,
    val cancelTicketAndMainMenu: String,
    val anotherOneTicket: String,
    val finalSendTicket: String,
    val doneTicket: String,
    val cancelTicket: String,
    val sendTicket: String,
    val ticketAskDescription: String,
    val ticketDescriptionAccepted: String,
    val ticketAttachFilesPrompt: String,
    val ticketMaxFilesError: String,
    val ticketNoTextAllowed: String,
    val ticketConfirmation: String,
    val ticketAttachFiles: String,
    val ticketSkipAttachFiles: String,
    val ticketCreatedMessage: String,
    val ticketsHeader: String,
    val ticketsEmpty: String,
    val ticketLine: String,
    val ticketAttachments: String,
    val ticketDocumentAdded: String,
    val ticketPhotoAdded: String,

    val reportTimeFormatError: String,
    val reportBadTimeError: String,
    val reportEmptyTickets: String,
    val reportNoAccessError: String
    )