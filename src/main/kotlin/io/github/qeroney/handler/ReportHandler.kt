package io.github.qeroney.handler

import io.github.dehuckakpyt.telegrambot.annotation.HandlerComponent
import io.github.dehuckakpyt.telegrambot.ext.container.fromId
import io.github.dehuckakpyt.telegrambot.factory.input.input
import io.github.dehuckakpyt.telegrambot.handler.BotHandler
import io.github.qeroney.config.properties.MessageTemplate
import io.github.qeroney.config.properties.ReportProperties
import io.github.qeroney.ext.generateExcelReport
import io.github.qeroney.service.ticket.TicketService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HandlerComponent
class ReportHandler(
    private val template: MessageTemplate,
    private val ticketService: TicketService,
    private val reportProperties: ReportProperties) : BotHandler({

    val chatIds = reportProperties.chatIds
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val parse: (String) -> LocalDate? = { str ->
        runCatching { LocalDate.parse(str, formatter) }.getOrNull()
    }

    command("/report_tickets") {
        if (fromId !in chatIds) {
            sendMessage(template.reportNoAccessError)
            return@command
        }

        val args = text.split(" ").filter { it.isNotBlank() }
        if (args.size != 3) {
            sendMessage(template.reportTimeFormatError)
            return@command
        }

        val dateFrom = parse(args[1])?.atStartOfDay() ?: sendMessage(template.reportBadTimeError with mapOf("args" to args[1])).let { return@command }
        val dateTo = parse(args[2])?.atTime(23, 59, 59) ?: sendMessage(template.reportBadTimeError with mapOf("args" to args[2])).let { return@command }

        val tickets = ticketService.getAllBySubmittedAtBetween(dateFrom, dateTo)

        if (tickets.isEmpty()) {
            sendMessage(template.reportEmptyTickets)
            return@command
        }

        val fileBytes = tickets.generateExcelReport()
        sendDocument(document = input("report_${args[1]}_${args[2]}.xlsx", fileBytes))
    }
})
