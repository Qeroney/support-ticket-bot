package io.github.qeroney.service.notification

import io.github.qeroney.config.FreeMarkerConfig
import io.github.qeroney.model.Attachment
import io.github.qeroney.model.Ticket
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.io.FilenameFilter
import java.io.StringWriter
import java.nio.file.Paths
import java.time.format.DateTimeFormatter

@Service
class SendNotificationService(
    private val mailSender: JavaMailSender,
    private val fmConfig: FreeMarkerConfig,
    @Value("\${spring.mail.username}") private val email: String) {

    fun sendTicketNotification(ticket: Ticket) {
        val subject = "Инцидент \"${ticket.owner.fullName}\""

        val vars = mapOf(
            "fio" to ticket.owner.fullName,
            "email" to ticket.owner.email,
            "phone" to ticket.owner.phone,
            "description" to ticket.description,
            "attachmentsCount" to ticket.attachmentCount,
            "createdAt" to ticket.submittedAt!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'МСК'")))

        val html = processTemplate("ticket-notification.ftl", vars)
        sendHtml(email, subject, html, ticket.files ?: emptyList())
    }

    fun sendVerificationCodeToEmail(email: String, code: String) {
        val html = processTemplate("verification-code.ftl", mapOf("code" to code))
        sendHtml(email, "Ваш код подтверждения", html)
    }

    private fun processTemplate(templateName: String, model: Map<String, *>): String =
        StringWriter().use { writer ->
            fmConfig.freemarkerConfiguration().getTemplate(templateName).process(model, writer)
            writer.toString()
        }

    private fun sendHtml(to: String, subject: String, htmlBody: String, attachments: List<Attachment> = emptyList()) {
        val msg = mailSender.createMimeMessage()
        MimeMessageHelper(msg, true, "UTF-8").apply {
            setFrom(email)
            setTo(to)
            setSubject(subject)
            setText(htmlBody, true)
            val dir = Paths.get("files", "attachments")
            attachments.forEach { attachment ->
                val filter = FilenameFilter { _, name -> name.startsWith(attachment.fileId + ".") }
                val matchedFiles = dir.toFile().listFiles(filter) ?: emptyArray()
                matchedFiles.forEach { file ->
                    val name = attachment.fileName ?: file.name
                    addAttachment(name, file)
                }
            }
        }
        mailSender.send(msg)
    }
}
