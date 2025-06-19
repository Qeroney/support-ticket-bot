package io.github.qeroney.service.notification

import io.github.qeroney.config.FreeMarkerConfig
import io.github.qeroney.model.Attachment
import io.github.qeroney.service.notification.argument.CreateTicketNotificationArgument
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.io.StringWriter
import java.nio.file.Paths

@Service
class SendNotificationService(
    private val mailSender: JavaMailSender,
    private val fmConfig: FreeMarkerConfig,
    @Value("\${spring.mail.username}") private val from: String) {

    private fun processTemplate(templateName: String, model: Map<String, Any>): String =
        StringWriter().use { writer ->
            fmConfig.freemarkerConfiguration().getTemplate(templateName).process(model, writer)
            writer.toString()
        }

    private fun sendHtml(to: String, subject: String, htmlBody: String, attachments: List<Attachment> = emptyList()) {
        val msg = mailSender.createMimeMessage()
        MimeMessageHelper(msg, true, "UTF-8").apply {
            setFrom(from)
            setTo(to)
            setSubject(subject)
            setText(htmlBody, true)
            attachments.forEach {
                Paths.get("files", "attachments", it.fileName).toFile().takeIf { f -> f.exists() }?.let { f ->
                    addAttachment(it.fileName ?: "attachment", f)
                }
            }
        }
        mailSender.send(msg)
    }

    fun sendTicketNotification(arg: CreateTicketNotificationArgument) {
        val html = processTemplate("ticket-notification.ftl", mapOf(
            "fio" to arg.fio,
            "email" to arg.email,
            "phone" to arg.phone,
            "description" to arg.description,
            "attachmentsCount" to arg.attachments.size,
            "createdAt" to arg.createdAt))
        sendHtml(arg.to, "Инцидент \"${arg.fio}\"", html, arg.attachments)
    }

    fun sendVerificationCodeToEmail(email: String, code: String) {
        val html = processTemplate("verification-code.ftl", mapOf("code" to code))
        sendHtml(email, "Ваш код подтверждения", html)
    }
}
